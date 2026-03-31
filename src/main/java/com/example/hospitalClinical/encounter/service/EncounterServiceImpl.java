package com.example.hospitalClinical.encounter.service;

import com.example.hospitalClinical.common.exception.BusinessException;
import com.example.hospitalClinical.common.exception.ErrorCode;
import com.example.hospitalClinical.common.client.external.billing.BillingApiClient;
import com.example.hospitalClinical.common.client.external.billing.BillingClinicalCompletedRequest;
import com.example.hospitalClinical.common.client.external.billing.BillingClinicalCompletedResult;
import com.example.hospitalClinical.common.client.internal.reception.ReceptionClient;
import com.example.hospitalClinical.common.client.internal.reception.ReceptionResponse;
import com.example.hospitalClinical.common.client.internal.reception.ReceptionStatusUpdateRequest;
import com.example.hospitalClinical.common.response.ApiResponse;
import com.example.hospitalClinical.encounter.dto.VisitCreateRequest;
import com.example.hospitalClinical.encounter.dto.VisitStartRequest;
import com.example.hospitalClinical.encounter.entity.Visit;
import com.example.hospitalClinical.encounter.entity.VisitQueue;
import com.example.hospitalClinical.encounter.entity.VisitStatusHistory;
import com.example.hospitalClinical.encounter.exception.VisitNotFoundException;
import com.example.hospitalClinical.encounter.repository.VisitQueueRepo;
import com.example.hospitalClinical.encounter.repository.VisitRepo;
import com.example.hospitalClinical.encounter.repository.VisitStatusHistoryRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EncounterServiceImpl implements EncounterService {

    private static final java.util.Set<String> STARTABLE_RECEPTION_STATUSES = java.util.Set.of("WAITING", "CALLED");

    private final VisitRepo visitRepo;
    private final VisitStatusHistoryRepo visitStatusHistoryRepo;
    private final VisitQueueRepo visitQueueRepo;
    private final ReceptionClient receptionClient;
    private final BillingApiClient billingApiClient;

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
        Long doctorId = reception.getDoctorId();
        if (patientId == null || doctorId == null) {
            throw new BusinessException(ErrorCode.RECEPTION_API_ERROR, "접수 정보에 환자/의사 정보가 없습니다.");
        }   //추가 검증 환자/의사
        // 중복 체크 (DB)//
        List<Visit> existing = visitRepo.findByReceptionIdAndVisitStatus(receptionId, "IN_PROGRESS");
            //DB조회(트랜잭션 포함)
        if (existing != null && !existing.isEmpty()) {
            throw new BusinessException(ErrorCode.VISIT_ALREADY_EXISTS_FOR_RECEPTION); //중복이면 종료 (트랜잭션 롤백
        }
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
            } else if (Visit.COMPLETED.equals(u)) {
                if (request.getStartTime() != null) {
                    v.start(request.getStartTime());
                } else {
                    v.start();
                }
                v.complete();
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
        try {
            v.applyAdministrativeVisitStatus(visitStatus);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_CLINICAL_STATUS);
        }
        Visit saved = visitRepo.save(v);
        if (Visit.COMPLETED.equals(saved.getVisitStatus())) {
            notifyBillingForCompletedVisit(saved);
        }
        return saved;
    }

    @Override
    @Transactional
    public Visit endVisit(Long visitId) {
        Visit v = visitRepo.findById(visitId).orElseThrow(VisitNotFoundException::new);
        v.complete();
        Visit saved = visitRepo.save(v);
        notifyBillingForCompletedVisit(saved);
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

    private void notifyBillingForCompletedVisit(Visit visit) {
        BillingClinicalCompletedRequest request = BillingClinicalCompletedRequest.builder()
                .eventId("clinical-completed-" + visit.getVisitId())
                .visitId(visit.getVisitId())
                .patientId(visit.getPatientId())
                .status(Visit.COMPLETED)
                .occurredAt(visit.getEndTime() != null ? visit.getEndTime() : visit.getUpdatedAt())
                .build();
        try {
            ApiResponse<BillingClinicalCompletedResult> response = billingApiClient.notifyClinicalCompleted(request);
            BillingClinicalCompletedResult result = response.getResult();
            log.info(
                    "[진료→수납] 완료 알림 전송 visitId={} success={} billId={} alreadyProcessed={}",
                    visit.getVisitId(),
                    response.isSuccess(),
                    result != null ? result.getBillId() : null,
                    result != null && result.isAlreadyProcessed()
            );
        } catch (Exception e) {
            log.warn("[진료→수납] 완료 알림 전송 실패 visitId={} message={}", visit.getVisitId(), e.getMessage());
        }
    }
}
