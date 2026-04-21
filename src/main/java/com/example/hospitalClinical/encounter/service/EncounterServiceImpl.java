package com.example.hospitalClinical.encounter.service;

import com.example.hospitalClinical.common.exception.BusinessException;
import com.example.hospitalClinical.common.exception.ErrorCode;
import com.example.hospitalClinical.common.client.external.billing.BillingClinicalClaimItem;
import com.example.hospitalClinical.common.client.external.billing.BillingClinicalCompletedRequest;
import com.example.hospitalClinical.common.integration.billing.kafka.BillingClinicalCompletedEventPublisher;
import com.example.hospitalClinical.common.client.internal.reception.ReceptionClient;
import com.example.hospitalClinical.common.client.internal.reception.ReceptionResponse;
import com.example.hospitalClinical.common.client.internal.reception.ReceptionStatusUpdateRequest;
import com.example.hospitalClinical.common.response.ApiResponse;
import com.example.hospitalClinical.encounter.dto.ClinicalVitalAssessResponse;
import com.example.hospitalClinical.encounter.dto.ClinicalVitalAssessSaveRequest;
import com.example.hospitalClinical.encounter.dto.VitalAssessSaveHistoryLine;
import com.example.hospitalClinical.encounter.dto.VisitCreateRequest;
import com.example.hospitalClinical.encounter.dto.VisitStartRequest;
import com.example.hospitalClinical.encounter.entity.ClinicalVitalAssess;
import com.example.hospitalClinical.encounter.entity.VitalSaveAudit;
import com.example.hospitalClinical.encounter.util.VitalAssessChangeSummarizer;
import com.example.hospitalClinical.encounter.entity.Visit;
import com.example.hospitalClinical.encounter.entity.VisitQueue;
import com.example.hospitalClinical.encounter.entity.VisitStatusHistory;
import com.example.hospitalClinical.encounter.exception.VisitNotFoundException;
import com.example.hospitalClinical.encounter.repository.ClinicalVitalAssessRepo;
import com.example.hospitalClinical.encounter.repository.VitalSaveAuditRepo;
import com.example.hospitalClinical.encounter.repository.VisitQueueRepo;
import com.example.hospitalClinical.encounter.repository.VisitRepo;
import com.example.hospitalClinical.encounter.repository.VisitStatusHistoryRepo;
import com.example.hospitalClinical.order.dto.OrderItemResponse;
import com.example.hospitalClinical.order.entity.Order;
import com.example.hospitalClinical.order.entity.OrderItem;
import com.example.hospitalClinical.order.service.OrderVisitService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EncounterServiceImpl implements EncounterService {

    private static final DateTimeFormatter VITAL_SAVE_AUDIT_AT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private static final java.util.Set<String> STARTABLE_RECEPTION_STATUSES = java.util.Set.of("WAITING", "CALLED");

    @Value("${app.stale-visit-auto-close.zone:Asia/Seoul}")
    private String staleVisitZoneId;

    private final VisitRepo visitRepo;
    private final ClinicalVitalAssessRepo clinicalVitalAssessRepo;
    private final VitalSaveAuditRepo vitalSaveAuditRepo;
    private final VisitStatusHistoryRepo visitStatusHistoryRepo;
    private final VisitQueueRepo visitQueueRepo;
    private final ReceptionClient receptionClient;
    private final BillingClinicalCompletedEventPublisher billingClinicalCompletedEventPublisher;
    private final OrderVisitService orderVisitService;
    private final PlatformTransactionManager transactionManager;

    private TransactionTemplate staleCloseTxn;

    @PostConstruct
    void initStaleCloseTxn() {
        staleCloseTxn = new TransactionTemplate(transactionManager);
        staleCloseTxn.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    @Transactional
    public Visit startVisit(VisitStartRequest request) {
         //접수 조회(외부API)-트랜잭션아님.//
        Long receptionId = request.getReceptionId();
        ReceptionResponse reception = receptionClient.getReception(receptionId);
        //상태 검증//
        String status = reception.getStatus() != null ? reception.getStatus().trim().toUpperCase() : "";

        if (!STARTABLE_RECEPTION_STATUSES.contains(status)) {
            throw new BusinessException(ErrorCode.RECEPTION_INVALID_STATUS);
        }       //조건 안 맞으면 여기서 종료(DB 변경 없음)
        Long patientId = reception.getPatientId();
        String doctorId = reception.getDoctorId();
        if (patientId == null || doctorId == null || doctorId.isBlank()) {
            throw new BusinessException(ErrorCode.RECEPTION_API_ERROR, "접수 정보에 환자/의사 정보가 없습니다.");
        }   //추가 검증 환자/의사
        // 중복 체크 (DB)//
        List<Visit> existing = visitRepo.findByReceptionIdAndVisitStatus(receptionId, "IN_PROGRESS");
            //DB조회(트랜잭션 포함)
        if (existing != null && !existing.isEmpty()) {
            throw new BusinessException(ErrorCode.VISIT_ALREADY_EXISTS_FOR_RECEPTION); //중복이면 종료 (트랜잭션 롤백
        }
        assertNoConcurrentVisit(doctorId, receptionId, null);
        //접수 상태 변경(외부 API)//
        ReceptionStatusUpdateRequest statusReq = new ReceptionStatusUpdateRequest();
        statusReq.setStatus("IN_PROGRESS");
        statusReq.setChangedBy(request.getChangedBy());
        statusReq.setReasonCode("VISIT_START");
        statusReq.setReasonText("진료 시작");
        receptionClient.updateReceptionStatus(receptionId, statusReq); //트랜잭션 아님./ 여기서 성공하면 롤백 불가.
        //visit 생성//
        Visit v = Visit.create(patientId, doctorId, receptionId);
        v.start();
                //아직 DB 저장 안됨.(객체만 생성)
        //Visit 저장(DB)//
        Visit saved = visitRepo.save(v);
        visitStatusHistoryRepo.save(VisitStatusHistory.create(saved.getVisitId(), Visit.IN_PROGRESS));
        return saved;
    }
        /* 부분 트랜잭션 상태(DB작업에 하나로 묶인다.)--> receptionClient.getReception(..)/updateReceptionStatus(..)
                                           => 트랜잭션 밖(다른 서버 호출) */
    @Override
    @Transactional
    public Visit createVisit(VisitCreateRequest request) {
        Visit v = Visit.create(
                request.getPatientId(),
                request.getDoctorId(),
                request.getReceptionId()
        );
        String raw = request.getVisitStatus();
        if (raw != null && !raw.isBlank()) {
            String u = raw.trim().toUpperCase();
            if (Visit.IN_PROGRESS.equals(u)) {
                if (request.getStartTime() != null) {
                    v.start(request.getStartTime());
                } else {
                    v.start();
                }
                assertNoConcurrentVisit(request.getDoctorId(), request.getReceptionId(), null);
            } else if (Visit.COMPLETED.equals(u)) {
                if (request.getStartTime() != null) {
                    v.start(request.getStartTime());
                } else {
                    v.start();
                }
                v.complete();
            } else if (Visit.AUTO_CLOSED.equals(u)) {
                if (request.getStartTime() != null) {
                    v.start(request.getStartTime());
                } else {
                    v.start();
                }
                assertNoConcurrentVisit(request.getDoctorId(), request.getReceptionId(), null);
                v.autoCloseStale(null);
            } else if (!Visit.WAITING.equals(u)) {
                throw new BusinessException(ErrorCode.INVALID_CLINICAL_STATUS);
            }
        } else if (request.getStartTime() != null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "visitStatus가 없으면 startTime을 지정할 수 없습니다.");
        }
        return visitRepo.save(v);
    }

    @Override
    public Visit getVisit(Long visitId) {
        return visitRepo.findById(visitId).orElseThrow(VisitNotFoundException::new);
    }

    @Override
    @Transactional
    public Visit updateVisitStatus(Long visitId, String visitStatus) {
        Visit v = visitRepo.findById(visitId).orElseThrow(VisitNotFoundException::new);
        String prev = v.getVisitStatus();
        try {
            v.applyAdministrativeVisitStatus(visitStatus);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_CLINICAL_STATUS);
        }
        if (Visit.IN_PROGRESS.equals(v.getVisitStatus())) {
            assertNoConcurrentVisit(v.getDoctorId(), v.getReceptionId(), visitId);
        }
        Visit saved = visitRepo.save(v);
        if (!Objects.equals(prev, saved.getVisitStatus())) {
            visitStatusHistoryRepo.save(VisitStatusHistory.create(saved.getVisitId(), saved.getVisitStatus()));
        }
        if (Visit.COMPLETED.equals(saved.getVisitStatus())) {
            notifyBillingCompleted(saved);
        }
        if (Visit.AUTO_CLOSED.equals(saved.getVisitStatus()) && Visit.IN_PROGRESS.equals(prev)) {
            try {
                ReceptionStatusUpdateRequest endReq = new ReceptionStatusUpdateRequest();
                endReq.setStatus("PAYMENT_WAIT");
                endReq.setReasonCode("VISIT_AUTO_CLOSED");
                endReq.setReasonText("미종료 진료 자동 마감");
                receptionClient.updateReceptionStatus(saved.getReceptionId(), endReq);
            } catch (Exception e) {
                log.warn(
                        "AUTO_CLOSED visit: reception update skipped visitId={} receptionId={} message={}",
                        saved.getVisitId(),
                        saved.getReceptionId(),
                        e.getMessage()
                );
            }
        }
        return saved;
    }

    @Override
    @Transactional
    public Visit endVisit(Long visitId) {
        Visit v = visitRepo.findById(visitId).orElseThrow(VisitNotFoundException::new);
        v.complete();
        Visit saved = visitRepo.save(v);
        ReceptionStatusUpdateRequest endReq = new ReceptionStatusUpdateRequest();
        endReq.setStatus("PAYMENT_WAIT");
        endReq.setReasonCode("VISIT_END");
        endReq.setReasonText("진료 완료 → 수납대기");
        receptionClient.updateReceptionStatus(saved.getReceptionId(), endReq);
        notifyBillingCompleted(saved);
        return saved;
    }

    @Override
    public List<Visit> listByPatientId(Long patientId) {
        return visitRepo.findByPatientIdOrderByStartTimeDesc(patientId);
    }

    @Override
    public List<Visit> listByReceptionId(Long receptionId) {
        return visitRepo.findByReceptionIdOrderByStartTimeDesc(receptionId);
    }

    @Override
    public List<Visit> listByStatus(String visitStatus) {
        return visitRepo.findByVisitStatusOrderByStartTimeAsc(visitStatus);
    }

    @Override
    public List<Visit> listByVisitStatuses(List<String> visitStatuses) {
        if (visitStatuses == null || visitStatuses.isEmpty()) {
            return List.of();
        }
        List<String> normalized =
                visitStatuses.stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(String::toUpperCase)
                        .distinct()
                        .collect(Collectors.toList());
        if (normalized.isEmpty()) {
            return List.of();
        }
        return visitRepo.findByVisitStatusInOrderByStartTimeAsc(normalized);
    }

    @Override
    public List<Visit> listAll() {
        return visitRepo.findAllByOrderByStartTimeDesc();
    }

    @Override
    @Transactional
    public VisitStatusHistory createStatusHistory(Long visitId, String status) {
        if (!visitRepo.existsById(visitId)) throw new VisitNotFoundException();
        return visitStatusHistoryRepo.save(VisitStatusHistory.create(visitId, status));
    }

    @Override
    public VisitStatusHistory getStatusHistory(Long historyId) {
        return visitStatusHistoryRepo.findById(historyId)
                .orElseThrow(() -> new IllegalArgumentException("VisitStatusHistory not found: " + historyId));
    }

    @Override
    public List<VisitStatusHistory> listStatusHistoryByVisitId(Long visitId) {
        return visitStatusHistoryRepo.findByVisitIdOrderByChangedAtDesc(visitId);
    }

    @Override
    @Transactional
    public VisitQueue createQueue(Long visitId, Integer queueOrder, Long roomId) {
        if (!visitRepo.existsById(visitId)) throw new VisitNotFoundException();
        return visitQueueRepo.save(VisitQueue.create(visitId, queueOrder, roomId));
    }

    @Override
    public VisitQueue getQueue(Long queueId) {
        return visitQueueRepo.findById(queueId)
                .orElseThrow(() -> new IllegalArgumentException("VisitQueue not found: " + queueId));
    }

    @Override
    public List<VisitQueue> listQueueByVisitId(Long visitId) {
        return visitQueueRepo.findByVisitIdOrderByQueueOrderAsc(visitId);
    }

    @Override
    public List<VisitQueue> listAllQueue() {
        return visitQueueRepo.findAllByOrderByQueueOrderAsc();
    }

    @Override
    public Optional<ClinicalVitalAssessResponse> getClinicalVitalAssessByVisitId(Long visitId) {
        visitRepo.findById(visitId).orElseThrow(VisitNotFoundException::new);
        return clinicalVitalAssessRepo
                .findByVisitId(visitId)
                .map(e -> ClinicalVitalAssessResponse.from(e, loadChartSaveHistoryLines(visitId)));
    }

    @Override
    @Transactional
    public ClinicalVitalAssessResponse upsertClinicalVitalAssess(Long visitId, ClinicalVitalAssessSaveRequest request) {
        Visit visit = visitRepo.findById(visitId).orElseThrow(VisitNotFoundException::new);
        assertVitalAssessSaveAllowed(visit);
        if (request.getVisitId() != null && !request.getVisitId().equals(visitId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "visitId가 경로와 일치하지 않습니다.");
        }
        ClinicalVitalAssess entity =
                clinicalVitalAssessRepo
                        .findByVisitId(visitId)
                        .orElseGet(() -> ClinicalVitalAssess.createNew(visitId, visit.getReceptionId()));
        VitalAssessChangeSummarizer.Snapshot snap = VitalAssessChangeSummarizer.Snapshot.from(entity);
        entity.applySave(request);
        ClinicalVitalAssess saved = clinicalVitalAssessRepo.save(entity);
        String changeSummary = VitalAssessChangeSummarizer.summarize(snap, saved);
        vitalSaveAuditRepo.save(VitalSaveAudit.create(visitId, saved.getRecordedAt(), changeSummary));
        return ClinicalVitalAssessResponse.from(saved, loadChartSaveHistoryLines(visitId));
    }

    private List<VitalAssessSaveHistoryLine> loadChartSaveHistoryLines(Long visitId) {
        try {
            return mapSaveAuditsToLines(vitalSaveAuditRepo.findByVisitIdOrderBySaveAuditIdAsc(visitId));
        } catch (Exception ex) {
            log.warn("vital save audit 목록 로드 실패 visitId={}", visitId, ex);
            return List.of();
        }
    }

    private void assertVitalAssessSaveAllowed(Visit visit) {
        if (visit.isTerminal()) {
            throw new BusinessException(
                    ErrorCode.VISIT_VITALS_EDIT_FORBIDDEN,
                    "종료된 진료에는 활력·문진을 저장할 수 없습니다. 새 접수 후 진료를 시작해 주세요.");
        }
        if (!Visit.IN_PROGRESS.equals(visit.getVisitStatus())) {
            return;
        }
        LocalDateTime dayStart = LocalDate.now(ZoneId.of(staleVisitZoneId)).atStartOfDay();
        LocalDateTime startOrCreated =
                visit.getStartTime() != null ? visit.getStartTime() : visit.getCreatedAt();
        if (startOrCreated != null && startOrCreated.isBefore(dayStart)) {
            throw new BusinessException(
                    ErrorCode.VISIT_VITALS_EDIT_FORBIDDEN,
                    "이전 일자에 시작된 진료 방문입니다. 새 접수 후 진료를 시작하거나, 해당 방문을 먼저 종료해 주세요.");
        }
    }

    private static List<VitalAssessSaveHistoryLine> mapSaveAuditsToLines(List<VitalSaveAudit> audits) {
        return audits.stream()
                .map(
                        a -> {
                            LocalDateTime t =
                                    a.getSavedAt() != null ? a.getSavedAt() : a.getRecordedAt();
                            String atStr =
                                    t == null ? "" : t.withNano(0).format(VITAL_SAVE_AUDIT_AT);
                            String det = a.getChangeSummary();
                            return new VitalAssessSaveHistoryLine(
                                    "진료 · 차트 저장",
                                    atStr,
                                    det != null && !det.isBlank() ? det : null);
                        })
                .collect(Collectors.toList());
    }

    @Override
    public int autoCloseStaleVisits(LocalDateTime dayStart) {
        List<Visit> list = visitRepo.findStaleInProgress(Visit.IN_PROGRESS, dayStart);
        int n = 0;
        for (Visit v : list) {
            Boolean ok =
                    staleCloseTxn.execute(
                            status -> tryAutoCloseVisit(v.getVisitId(), dayStart));
            if (Boolean.TRUE.equals(ok)) {
                n++;
            }
        }
        return n;
    }

    private boolean tryAutoCloseVisit(Long visitId, LocalDateTime dayStart) {
        Visit v = visitRepo.findById(visitId).orElse(null);
        if (v == null || !Visit.IN_PROGRESS.equals(v.getVisitStatus())) {
            return false;
        }
        LocalDateTime startOrCreated =
                v.getStartTime() != null ? v.getStartTime() : v.getCreatedAt();
        if (startOrCreated == null || !startOrCreated.isBefore(dayStart)) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        v.autoCloseStale(now);
        visitRepo.save(v);
        visitStatusHistoryRepo.save(VisitStatusHistory.create(v.getVisitId(), Visit.AUTO_CLOSED));
        try {
            ReceptionStatusUpdateRequest endReq = new ReceptionStatusUpdateRequest();
            endReq.setStatus("PAYMENT_WAIT");
            endReq.setReasonCode("VISIT_AUTO_CLOSED");
            endReq.setReasonText("미종료 진료 자동 마감");
            receptionClient.updateReceptionStatus(v.getReceptionId(), endReq);
        } catch (Exception e) {
            log.warn(
                    "stale visit auto-close: reception status update skipped visitId={} receptionId={} message={}",
                    v.getVisitId(),
                    v.getReceptionId(),
                    e.getMessage()
            );
        }
        return true;
    }

    private void assertNoConcurrentVisit(String doctorId, Long receptionId, Long exceptVisitId) {
        if (doctorId == null || doctorId.isBlank() || receptionId == null) {
            return;
        }
        for (Visit ov : visitRepo.findByVisitStatusAndDoctorId(Visit.IN_PROGRESS, doctorId.trim())) {
            if (exceptVisitId != null && exceptVisitId.equals(ov.getVisitId())) {
                continue;
            }
            if (!receptionId.equals(ov.getReceptionId())) {
                throw new BusinessException(ErrorCode.DOCTOR_VISIT_ALREADY_IN_PROGRESS);
            }
        }
    }

    private void notifyBillingCompleted(Visit visit) {
        List<BillingClinicalClaimItem> items = buildClaimLines(visit.getVisitId());
        if (items.isEmpty()) {
            log.warn("[진료→수납] 청구 품목이 없어 claims 전송 생략 visitId={}", visit.getVisitId());
            return;
        }
        BillingClinicalCompletedRequest request = BillingClinicalCompletedRequest.builder()
                .eventId("clinical-completed-" + visit.getVisitId())
                .visitId(visit.getVisitId())
                .patientId(visit.getPatientId())
                .status(Visit.COMPLETED)
                .occurredAt(visit.getEndTime() != null ? visit.getEndTime() : visit.getUpdatedAt())
                .items(items)
                .build();
        // REST → Kafka 전환: 수납에는 진료완료 이벤트를 발행하고, 수납 서비스가 이를 구독하여 청구 생성 처리합니다.
        billingClinicalCompletedEventPublisher.publish(request);
        log.info("[진료→수납] 완료 이벤트 발행 visitId={} itemCount={}", visit.getVisitId(), items.size());
    }

    private List<BillingClinicalClaimItem> buildClaimLines(Long visitId) {
        List<Order> orders = orderVisitService.listOrdersByVisitId(visitId);
        List<BillingClinicalClaimItem> out = new ArrayList<>();
        for (Order order : orders) {
            if (order.getOrderStatus() != null && "CANCELLED".equalsIgnoreCase(order.getOrderStatus().trim())) {
                continue;
            }
            String orderType =
                    order.getOrderType() != null ? order.getOrderType().name() : "UNKNOWN";
            for (OrderItem line : order.getItems()) {
                Long sourceId = line.getOrderItemId();
                if (sourceId == null) {
                    continue;
                }
                String name =
                        OrderItemResponse.stripEncodedOrderItemSuffix(
                                line.getItemDetailCode() != null ? line.getItemDetailCode() : "");
                String code = line.getItemCode() != null ? line.getItemCode() : "";
                out.add(
                        BillingClinicalClaimItem.builder()
                                .itemName(name)
                                .itemCode(code)
                                .orderType(orderType)
                                .sourceId(sourceId)
                                .sourceType(BillingClinicalClaimItem.SOURCE_TYPE_CLINICAL_ORDER_ITEM)
                                .build());
            }
        }
        return out;
    }
}
