package com.example.hospitalClinical.order.service;

import com.example.hospitalClinical.common.client.external.clinicalsupport.MedicationRecordOutboundRequest;
import com.example.hospitalClinical.common.client.external.clinicalsupport.TreatmentResultOutboundRequest;
import com.example.hospitalClinical.common.client.internal.reception.ReceptionClient;
import com.example.hospitalClinical.common.client.internal.reception.ReceptionResponse;
import com.example.hospitalClinical.common.exception.BusinessException;
import com.example.hospitalClinical.common.exception.ErrorCode;
import com.example.hospitalClinical.documentation.entity.SoapRx;
import com.example.hospitalClinical.documentation.repository.SoapRxRepo;
import com.example.hospitalClinical.documentation.service.DocumentationService;
import com.example.hospitalClinical.encounter.entity.Visit;
import com.example.hospitalClinical.encounter.exception.VisitNotFoundException;
import com.example.hospitalClinical.encounter.repository.VisitRepo;
import com.example.hospitalClinical.order.dto.MedicationRecordCreateRequest;
import com.example.hospitalClinical.order.dto.MedicationRecordResponse;
import com.example.hospitalClinical.order.dto.OrderCreateRequest;
import com.example.hospitalClinical.order.dto.OrderItemCreateRequest;
import com.example.hospitalClinical.order.dto.OrderItemResponse;
import com.example.hospitalClinical.order.dto.OrderResponse;
import com.example.hospitalClinical.order.dto.TreatmentResultCreateRequest;
import com.example.hospitalClinical.order.dto.TreatmentResultResponse;
import com.example.hospitalClinical.order.entity.MedicationRecord;
import com.example.hospitalClinical.order.entity.Order;
import com.example.hospitalClinical.order.entity.OrderItem;
import com.example.hospitalClinical.order.entity.OrderResult;
import com.example.hospitalClinical.order.entity.OrderType;
import com.example.hospitalClinical.order.entity.TreatmentResult;
import com.example.hospitalClinical.order.event.LabOrderCommittedEvent;
import com.example.hospitalClinical.order.exception.OrderNotFoundException;
import com.example.hospitalClinical.order.repository.MedicationRecordRepo;
import com.example.hospitalClinical.order.repository.OrderItemRepo;
import com.example.hospitalClinical.order.repository.OrderRepo;
import com.example.hospitalClinical.order.repository.OrderResultRepo;
import com.example.hospitalClinical.order.repository.TreatmentResultRepo;
import com.example.hospitalClinical.order.integration.clinicalsupport.kafka.ClinicalSupportOrderEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service    //스프링 서비스 계층(비즈니스 로직 담당)
@RequiredArgsConstructor    //final 필드 자동 생성자 주입(DI)
@Slf4j
@Transactional(readOnly = true) //기본은 조회 전용(성능 최적화)
public class OrderVisitServiceImpl implements OrderVisitService {
            //final-> 값 변경 불가
    private final OrderRepo orderRepo;  //Order 엔터티 DB 접근.(오더 생성/조회/삭제)
    private final OrderItemRepo orderItemRepo;  //OrderItem DB접근.
    private final OrderResultRepo orderResultRepo;  //검사결과 DB 접근.(결과값 저장/조회)
    private final VisitRepo visitRepo;  //visit 존재 여부 검증.(visitId 유효한지 체크)
    private final ApplicationEventPublisher eventPublisher; // 이벤트 발행:검사 오더 요청-->진료 서비스에게 전달
    private final SoapRxRepo soapRxRepo;    // 기존 SOAP 처방 데이터 조회.(레거시)
    private final DocumentationService documentationService;
    private final ReceptionClient receptionClient;
    private final MedicationRecordRepo medicationRecordRepo;
    private final TreatmentResultRepo treatmentResultRepo;
    private final ClinicalSupportOrderEventPublisher clinicalSupportOrderEventPublisher;

    private static final Set<String> SUPPORT_ALLOWED_ORDER_STATUS =
            Set.of("REQUESTED", "IN_PROGRESS", "COMPLETED", "CANCELLED");   //

    @Override
    public List<OrderResponse> listOrders(Long visitId, String orderTypeFilterOrNull) {
        String f = orderTypeFilterOrNull == null ? "" : orderTypeFilterOrNull.trim();
        if (f.isEmpty()) {
            return listOrdersByVisitId(visitId).stream()
                    .map(order -> OrderResponse.from(order))
                    .collect(Collectors.toList());
        }
        String u = f.toUpperCase();
        if (!"PRESCRIPTION".equals(u) && !"TEST".equals(u) && !"TREATMENT".equals(u)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        if ("PRESCRIPTION".equals(u)) {
            return listPrescriptionOrdersMerged(visitId);
        }
        return listOrdersByVisitId(visitId).stream()
                .filter(o -> o.getOrderType() != null && o.getOrderType().matchesApiOrderTypeFilter(u))
                .map(order -> OrderResponse.from(order))
                .collect(Collectors.toList());
    }

    private List<OrderResponse> listPrescriptionOrdersMerged(Long visitId) {
        List<AbstractMap.SimpleEntry<OrderResponse, Instant>> keyed = new ArrayList<>();
        for (Order o : listOrdersByVisitId(visitId)) {
            if (o.getOrderType() == null || !o.getOrderType().isPrescription()) {
                continue;
            }
            if (isCancelledForList(o.getOrderStatus())) {
                continue;
            }
            for (OrderItem item : o.getItems()) {
                keyed.add(new AbstractMap.SimpleEntry<>(singleItemOrderViewFromEntity(o, item), sortInstant(o, item)));
            }
        }
        for (SoapRx rx : soapRxRepo.findByVisitIdOrderByPrescriptionIdAsc(visitId)) {
            if (!orderRepo.existsByLegacyPrescriptionId(rx.getPrescriptionId())) {
                keyed.add(new AbstractMap.SimpleEntry<>(legacySoapOrderResponse(rx), soapRxInstant(rx)));
            }
        }
        keyed.sort(
                Comparator.comparing(
                                (AbstractMap.SimpleEntry<OrderResponse, Instant> entry) -> entry.getValue())
                        .reversed());
        return keyed.stream()
                .map((AbstractMap.SimpleEntry<OrderResponse, Instant> entry) -> entry.getKey())
                .collect(Collectors.toList());
    }

    private static OrderResponse singleItemOrderViewFromEntity(Order o, OrderItem item) {
        OrderItemResponse ir = OrderItemResponse.from(item);
        return new OrderResponse(
                o.getOrderId(),
                o.getVisitId(),
                o.getOrderType() != null ? o.getOrderType().name() : null,
                o.getOrderStatus(),
                o.getDoctorId(),
                o.getOrderDate(),
                o.getCreatedAt(),
                o.getUpdatedAt(),
                List.of(ir));
    }

    @Override
    @Transactional
    public OrderResponse createOrder(Long visitId, OrderCreateRequest request) {
        Order saved = persistNewOrder(visitId, request);
        return OrderResponse.from(saved);
    }

    @Override
    @Transactional
    public OrderItemResponse updateOrderItemLine(
            Long visitId, Long orderId, Long orderItemId, OrderItemCreateRequest body) {
        if (isLegacySoapKey(orderId, orderItemId)) {
            long legacyId = -orderId;
            documentationService.updateSoapRx(
                    visitId,
                    legacyId,
                    body != null ? body.getItemName() : null,
                    body != null ? body.getDosage() : null,
                    body != null ? body.getFrequency() : null,
                    body != null ? body.getDuration() : null);
            SoapRx rx = soapRxRepo
                    .findByPrescriptionIdAndVisitId(legacyId, visitId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));
            return legacySoapItemResponse(rx);
        }
        Order o = getOrder(orderId);
        if (!o.getVisitId().equals(visitId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        if (o.getOrderType() != null && o.getOrderType().isPrescription()) {
            return OrderItemResponse.from(updatePrescriptionItem(visitId, orderId, orderItemId, body));
        }
        return OrderItemResponse.from(updateOrderItem(orderItemId, body));
    }

    @Override
    @Transactional
    public void deleteOrderItemLine(Long visitId, Long orderId, Long orderItemId) {
        if (isLegacySoapKey(orderId, orderItemId)) {
            documentationService.removeSoapRx(visitId, -orderId);
            return;
        }
        Order o = getOrder(orderId);
        if (!o.getVisitId().equals(visitId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        if (o.getOrderType() != null && o.getOrderType().isPrescription()) {
            deletePrescriptionItem(visitId, orderId, orderItemId);
        } else {
            deleteOrderItem(orderId, orderItemId);
        }
    }

    @Override
    public Order getOrder(Long orderId) {
        return orderRepo.findByIdWithItems(orderId).orElseThrow(() -> new OrderNotFoundException());
    }

    @Override
    public List<Order> listOrdersByVisitId(Long visitId) {
        if (!visitRepo.existsById(visitId)) {
            return List.of();
        }
        return orderRepo.findByVisitIdOrderByOrderDateDesc(visitId);
    }

    @Override
    @Transactional
    public Order updateOrderStatus(Long visitId, Long orderId, String orderStatus) {
        if (orderStatus == null || orderStatus.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        String next = orderStatus.trim().toUpperCase();
        if (!"CANCELLED".equals(next)) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_CLINICIAN_FORBIDDEN);
        }
        Order o = requireOrderForVisit(visitId, orderId);
        assertClinicianMayCancel(o);
        o.setOrderStatus("CANCELLED");
        return orderRepo.save(o);
    }

    @Override
    @Transactional
    public Order cancelOrder(Long visitId, Long orderId) {
        return updateOrderStatus(visitId, orderId, "CANCELLED");
    }

    @Override
    @Transactional
    public Order syncOrderStatusFromSupport(Long visitId, Long orderId, String orderStatus) {
        if (orderStatus == null || orderStatus.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        String next = orderStatus.trim().toUpperCase();
        if ("REQUEST".equals(next)) {
            next = "REQUESTED";
        }
        if (!SUPPORT_ALLOWED_ORDER_STATUS.contains(next)) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        Order o = requireOrderForVisit(visitId, orderId);
        o.setOrderStatus(next);
        return orderRepo.save(o);
    }

    @Override
    @Transactional
    public OrderItem createOrderItem(Long orderId, OrderItemCreateRequest request) {
        Order order = orderRepo.findById(orderId).orElseThrow(() -> new OrderNotFoundException());
        validateOrderItems(order.getOrderType(), List.of(request));
        Visit visit = visitRepo.findById(order.getVisitId()).orElseThrow(VisitNotFoundException::new);
        NamePair np = resolvePatientAndDepartment(order.getVisitId(), visit.getReceptionId(), null, null);
        if (visit.getPatientId() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        OrderItem item =
                toOrderItem(order.getOrderType(), request, visit.getPatientId(), np.patientName(), np.departmentName());
        order.addItem(item);
        orderRepo.saveAndFlush(order);
        if (order.getOrderType() != null
                && order.getOrderType().isLabCommittedType()
                && item.getOrderItemId() != null) {
            eventPublisher.publishEvent(
                    new LabOrderCommittedEvent(order.getOrderType(), List.of(item.getOrderItemId())));
        }
        return item;
    }

    @Override
    public OrderItem getOrderItem(Long orderItemId) {
        return orderItemRepo
                .findWithOrderByOrderItemId(orderItemId)
                .orElseGet(
                        () ->
                                orderItemRepo
                                        .findById(orderItemId)
                                        .orElseThrow(
                                                () ->
                                                        new IllegalArgumentException(
                                                                "OrderItem not found: " + orderItemId)));
    }

    @Override
    public List<OrderItem> listOrderItemsByOrderId(Long orderId) {
        return orderItemRepo.findByOrder_OrderId(orderId);
    }

    @Override
    @Transactional
    public OrderItem updateOrderItem(Long orderItemId, OrderItemCreateRequest request) {
        OrderItem item = getOrderItem(orderItemId);
        Order ord = item.getOrder();
        if (ord != null && ord.getOrderType() != null && ord.getOrderType().isPrescription()) {
            if (request.getItemCode() != null) {
                item.setItemCode(trimToNull(request.getItemCode()));
            }
            if (request.getItemDetailCode() != null) {
                item.setItemDetailCode(request.getItemDetailCode().trim());
            } else {
                OrderItemResponse.PrescriptionLineFields cur =
                        OrderItemResponse.decodePrescriptionLineFields(item.getItemDetailCode());
                String nextName =
                        request.getItemName() != null
                                ? OrderItemResponse.stripEncodedOrderItemSuffix(request.getItemName().trim())
                                : cur.displayName();
                String nextDosage =
                        request.getDosage() != null ? trimToNull(request.getDosage()) : cur.dosage();
                String nextFreq =
                        request.getFrequency() != null ? trimToNull(request.getFrequency()) : cur.frequency();
                String nextDur =
                        request.getDuration() != null ? trimToNull(request.getDuration()) : cur.duration();
                item.setItemDetailCode(
                        OrderItemResponse.encodePrescriptionLineFields(
                                nextName, nextDosage, nextFreq, nextDur));
            }
            return orderItemRepo.save(item);
        }
        if (request.getItemCode() != null) {
            item.setItemCode(trimToNull(request.getItemCode()));
        }
        if (request.getItemDetailCode() != null) {
            item.setItemDetailCode(request.getItemDetailCode().trim());
        } else if (request.getItemName() != null) {
            item.setItemDetailCode(OrderItemResponse.stripEncodedOrderItemSuffix(request.getItemName().trim()));
        }
        return orderItemRepo.save(item);
    }

    @Override
    @Transactional
    public void deleteOrderItem(Long orderId, Long orderItemId) {
        Order order = orderRepo.findByIdWithItems(orderId).orElseThrow(() -> new OrderNotFoundException());
        order.getItems().removeIf(i -> i.getOrderItemId().equals(orderItemId));
        if (order.getItems().isEmpty()) {
            orderRepo.delete(order);
        } else {
            orderRepo.save(order);
        }
    }

    @Override
    @Transactional
    public OrderItem updatePrescriptionItem(
            Long visitId, Long orderId, Long orderItemId, OrderItemCreateRequest request) {
        Order o = requireOrderForVisit(visitId, orderId);
        if (o.getOrderType() == null || !o.getOrderType().isPrescription()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        OrderItem item = getOrderItem(orderItemId);
        if (item.getOrder() == null || !item.getOrder().getOrderId().equals(orderId)) {
            throw new OrderNotFoundException();
        }
        OrderItem updated = updateOrderItem(orderItemId, request);
        Order refreshed = requireOrderForVisit(visitId, orderId);
        if (refreshed.getLegacyPrescriptionId() == null) {
            attachSoapPrescriptionMirror(visitId, refreshed);
        } else {
            syncLinkedSoapRxAfterPrescriptionItemSave(visitId, refreshed, updated);
        }
        return updated;
    }

    private void syncLinkedSoapRxAfterPrescriptionItemSave(Long visitId, Order order, OrderItem item) {
        Long lid = order.getLegacyPrescriptionId();
        if (lid == null) {
            return;
        }
        OrderItemResponse.PrescriptionLineFields f =
                OrderItemResponse.decodePrescriptionLineFields(item.getItemDetailCode());
        documentationService.replaceSoapPrescriptionFromOrder(
                visitId, lid, f.displayName(), f.dosage(), f.frequency(), f.duration());
    }

    @Override
    @Transactional
    public void deletePrescriptionItem(Long visitId, Long orderId, Long orderItemId) {
        Order o = requireOrderForVisit(visitId, orderId);
        if (o.getOrderType() == null || !o.getOrderType().isPrescription()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        OrderItem item = getOrderItem(orderItemId);
        if (item.getOrder() == null || !item.getOrder().getOrderId().equals(orderId)) {
            throw new OrderNotFoundException();
        }
        Long legacyRx = o.getLegacyPrescriptionId();
        deleteOrderItem(orderId, orderItemId);
        if (legacyRx != null && !orderRepo.existsById(orderId)) {
            documentationService.removeSoapRx(visitId, legacyRx);
        }
    }

    @Override
    @Transactional
    public OrderResult createOrderResult(Long orderItemId, String resultValue, String resultStatus) {
        if (!orderItemRepo.existsById(orderItemId)) {
            throw new IllegalArgumentException("OrderItem not found: " + orderItemId);
        }
        return orderResultRepo.save(OrderResult.create(orderItemId, resultValue, resultStatus));
    }

    @Override
    public OrderResult getOrderResult(Long resultId) {
        return orderResultRepo
                .findById(resultId)
                .orElseThrow(() -> new IllegalArgumentException("OrderResult not found: " + resultId));
    }

    @Override
    public List<OrderResult> listOrderResultsByOrderItemId(Long orderItemId) {
        return orderResultRepo.findByOrderItemIdOrderByResultDateDesc(orderItemId);
    }

    @Override
    @Transactional
    public OrderResult updateOrderResult(Long resultId, String resultValue, String resultStatus) {
        OrderResult r = getOrderResult(resultId);
        if (resultValue != null) {
            r.setResultValue(resultValue);
        }
        if (resultStatus != null) {
            r.setResultStatus(resultStatus);
        }
        return orderResultRepo.save(r);
    }

    @Override
    @Transactional
    public MedicationRecordResponse createMedicationRecord(Long visitId, MedicationRecordCreateRequest request) {
        Visit visit = visitRepo.findById(visitId).orElseThrow(VisitNotFoundException::new);
        String medicationId = newMedicationRecordId();
        NamePair names =
                resolvePatientAndDepartment(
                        visitId,
                        visit.getReceptionId(),
                        request.getPatientName(),
                        request.getDepartmentName());
        String patientName = names.patientName();
        String departmentName = names.departmentName();
        BigDecimal doseNumber = BigDecimal.valueOf(request.getDoseNumber());
        MedicationRecord entity = MedicationRecord.create(
                medicationId,
                visit.getPatientId(),
                patientName,
                departmentName,
                doseNumber,
                request.getDoseUnit() != null ? request.getDoseUnit().trim() : null,
                trimToNull(request.getDoseKind()),
                request.getStatus());
        medicationRecordRepo.save(entity);
        try {
            MedicationRecordOutboundRequest outbound = MedicationRecordOutboundRequest.builder()
                    .medicationId(entity.getMedicationId())
                    .patientId(entity.getPatientId())
                    .patientName(entity.getPatientName())
                    .departmentName(entity.getDepartmentName())
                    .doseNumber(request.getDoseNumber())
                    .doseUnit(entity.getDoseUnit())
                    .doseKind(entity.getDoseKind())
                    .progressStatus(entity.getStatus())
                    .build();
            clinicalSupportOrderEventPublisher.publishMedicationRecord(outbound);
        } catch (Exception e) {
            log.warn("진료지원 투약 연동 실패 medicationId={}", medicationId, e);
        }
        return MedicationRecordResponse.from(entity);
    }

    @Override
    @Transactional
    public TreatmentResultResponse createTreatmentResult(Long visitId, TreatmentResultCreateRequest request) {
        Visit visit = visitRepo.findById(visitId).orElseThrow(VisitNotFoundException::new);
        String procedureResultId = newTreatmentResultId();
        NamePair names =
                resolvePatientAndDepartment(
                        visitId,
                        visit.getReceptionId(),
                        request.getPatientName(),
                        request.getDepartmentName());
        String patientName = names.patientName();
        String departmentName = names.departmentName();
        String detail = trimToNull(request.getDetail());
        if (detail == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        TreatmentResult entity = TreatmentResult.create(
                procedureResultId,
                visit.getPatientId(),
                patientName,
                departmentName,
                request.getStatus(),
                detail);
        treatmentResultRepo.save(entity);
        try {
            TreatmentResultOutboundRequest outbound = TreatmentResultOutboundRequest.builder()
                    .procedureResultId(entity.getProcedureResultId())
                    .patientId(entity.getPatientId())
                    .patientName(entity.getPatientName())
                    .departmentName(entity.getDepartmentName())
                    .progressStatus(entity.getStatus())
                    .detail(entity.getDetail())
                    .build();
            clinicalSupportOrderEventPublisher.publishTreatmentResult(outbound);
        } catch (Exception e) {
            log.warn("진료지원 처치 연동 실패 procedureResultId={}", procedureResultId, e);
        }
        return TreatmentResultResponse.from(entity);
    }

    @Override
    public List<MedicationRecordResponse> listMedicationRecordsByVisit(Long visitId) {
        Visit visit = visitRepo.findById(visitId).orElseThrow(VisitNotFoundException::new);
        return medicationRecordRepo.findByPatientIdOrderByCreatedAtDesc(visit.getPatientId()).stream()
                .map(MedicationRecordResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<TreatmentResultResponse> listTreatmentResultsByVisit(Long visitId) {
        Visit visit = visitRepo.findById(visitId).orElseThrow(VisitNotFoundException::new);
        return treatmentResultRepo.findByPatientIdOrderByCreatedAtDesc(visit.getPatientId()).stream()
                .map(TreatmentResultResponse::from)
                .collect(Collectors.toList());
    }

    private String newMedicationRecordId() {
        String prefix = "MED" + LocalDate.now(ZoneId.systemDefault()).format(DateTimeFormatter.BASIC_ISO_DATE) + "-";
        return medicationRecordRepo
                .findLatestByMedicationIdPrefix(prefix)
                .map(MedicationRecord::getMedicationId)
                .map(OrderVisitServiceImpl::bumpDailySequenceSuffix)
                .orElse(prefix + "0001");
    }

    private String newTreatmentResultId() {
        String prefix = "TR" + LocalDate.now(ZoneId.systemDefault()).format(DateTimeFormatter.BASIC_ISO_DATE) + "-";
        return treatmentResultRepo
                .findLatestByProcedureResultIdPrefix(prefix)
                .map(TreatmentResult::getProcedureResultId)
                .map(OrderVisitServiceImpl::bumpDailySequenceSuffix)
                .orElse(prefix + "0001");
    }

    private static String bumpDailySequenceSuffix(String fullId) {
        int dash = fullId.lastIndexOf('-');
        if (dash < 0 || dash >= fullId.length() - 1) {
            throw new IllegalStateException("invalid id: " + fullId);
        }
        int n = Integer.parseInt(fullId.substring(dash + 1), 10);
        if (n >= 9999) {
            throw new IllegalStateException("daily id overflow: " + fullId);
        }
        return fullId.substring(0, dash + 1) + String.format("%04d", n + 1);
    }

    private NamePair resolvePatientAndDepartment(
            Long visitId,
            Long receptionId,
            String requestPatientName,
            String requestDepartmentName) {
        String patientName = trimToNull(requestPatientName);
        String departmentName = trimToNull(requestDepartmentName);
        if (patientName != null && departmentName != null) {
            return new NamePair(patientName, departmentName);
        }
        try {
            ReceptionResponse r = receptionClient.getReception(receptionId);
            if (patientName == null) {
                patientName = trimToNull(r.getPatientName());
            }
            if (departmentName == null) {
                departmentName = trimToNull(r.getDepartmentName());
            }
        } catch (Exception e) {
            log.warn("접수 조회 실패 visitId={} receptionId={}", visitId, receptionId, e);
        }
        return new NamePair(patientName, departmentName);
    }

    private record NamePair(String patientName, String departmentName) {}

    private Order persistNewOrder(Long visitId, OrderCreateRequest request) {
        Visit visit = visitRepo.findById(visitId).orElseThrow(VisitNotFoundException::new);
        NamePair np = resolvePatientAndDepartment(visitId, visit.getReceptionId(), null, null);
        Long patientId = visit.getPatientId();
        if (patientId == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        OrderType orderType;
        try {
            orderType = OrderType.fromApi(request.getOrderType());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        if (orderType == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        List<OrderItemCreateRequest> items = request.getItems() != null ? request.getItems() : List.of();
        if (orderType.isPrescription()) {
            if (items.isEmpty()) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST);
            }
            validateOrderItems(orderType, items);
        } else if (!items.isEmpty()) {
            validateOrderItems(orderType, items);
        }
        String doctorId = trimToNull(request.getDoctorId());
        if (doctorId == null) {
            doctorId = trimToNull(visit.getDoctorId());
        }
        if (doctorId == null && visit.getReceptionId() != null) {
            try {
                ReceptionResponse rec = receptionClient.getReception(visit.getReceptionId());
                if (rec != null) {
                    doctorId = trimToNull(rec.getDoctorId());
                }
            } catch (Exception e) {
                log.warn("reception doctorId fallback failed visitId={} receptionId={}", visitId, visit.getReceptionId(), e);
            }
        }
        Order o = Order.create(visitId, orderType, "REQUESTED", doctorId);
        for (OrderItemCreateRequest req : items) {
            o.addItem(toOrderItem(orderType, req, patientId, np.patientName(), np.departmentName()));
        }
        Order saved = orderRepo.saveAndFlush(o);
        publishLabOrderCommittedIfNeeded(saved);
        if (orderType.isPrescription()) {
            attachSoapPrescriptionMirror(visitId, saved);
        }
        return saved;
    }

    private void attachSoapPrescriptionMirror(Long visitId, Order order) {
        if (order.getLegacyPrescriptionId() != null) {
            return;
        }
        if (order.getItems() == null || order.getItems().isEmpty()) {
            return;
        }
        OrderItem first = order.getItems().get(0);
        OrderItemResponse.PrescriptionLineFields f =
                OrderItemResponse.decodePrescriptionLineFields(first.getItemDetailCode());
        if (f.displayName() == null || f.displayName().isBlank()) {
            return;
        }
        Long rxId =
                documentationService.saveSoapPrescriptionRow(
                        visitId,
                        f.displayName(),
                        f.dosage(),
                        f.frequency(),
                        f.duration());
        order.setLegacyPrescriptionId(rxId);
        orderRepo.save(order);
    }

    private Order requireOrderForVisit(Long visitId, Long orderId) {
        Order o = orderRepo.findByIdWithItems(orderId).orElseThrow(() -> new OrderNotFoundException());
        if (!o.getVisitId().equals(visitId)) {
            throw new VisitNotFoundException();
        }
        return o;
    }

    private static void assertClinicianMayCancel(Order o) {
        String s = normalizeOrderStatusForRule(o.getOrderStatus());
        if ("COMPLETED".equals(s) || "CANCELLED".equals(s)) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_NOT_CANCELLABLE);
        }
    }

    private static String normalizeOrderStatusForRule(String raw) {
        if (raw == null || raw.isBlank()) {
            return "REQUESTED";
        }
        String s = raw.trim().toUpperCase();
        return "REQUEST".equals(s) ? "REQUESTED" : s;
    }

    private void publishLabOrderCommittedIfNeeded(Order order) {
        if (order.getOrderType() == null
                || !order.getOrderType().isLabCommittedType()
                || order.getItems() == null
                || order.getItems().isEmpty()) {
            return;
        }
        List<Long> ids = order.getItems().stream()
                .map(item -> item.getOrderItemId())
                .filter(id -> id != null)
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            return;
        }
        eventPublisher.publishEvent(new LabOrderCommittedEvent(order.getOrderType(), ids));
    }

    private static void validateOrderItems(OrderType orderType, List<OrderItemCreateRequest> items) {
        if (orderType == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        if (items == null || items.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        if (orderType.isPrescription()) {
            for (OrderItemCreateRequest req : items) {
                if (req.getItemName() == null || req.getItemName().isBlank()) {
                    throw new BusinessException(ErrorCode.INVALID_REQUEST);
                }
            }
        } else if (orderType.isTestCategory()) {
            for (OrderItemCreateRequest req : items) {
                if (trimToNull(req.getItemDetailCode()) == null && trimToNull(req.getItemCode()) == null) {
                    throw new BusinessException(ErrorCode.INVALID_REQUEST);
                }
            }
        } else {
            for (OrderItemCreateRequest req : items) {
                if (req.getItemCode() == null || req.getItemCode().isBlank()) {
                    throw new BusinessException(ErrorCode.INVALID_REQUEST);
                }
            }
        }
    }

    private static OrderItem toOrderItem(
            OrderType orderType,
            OrderItemCreateRequest req,
            Long patientId,
            String patientName,
            String departmentName) {
        if (orderType == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        if (patientId == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        if (orderType.isPrescription()) {
            String nm = OrderItemResponse.stripEncodedOrderItemSuffix(req.getItemName().trim());
            String detail =
                    OrderItemResponse.encodePrescriptionLineFields(
                            nm, trimToNull(req.getDosage()), trimToNull(req.getFrequency()), trimToNull(req.getDuration()));
            return OrderItem.createPrescriptionLine(detail, patientId, patientName, departmentName);
        }
        if (orderType.isTestCategory()) {
            String groupCode = labGroupCodeForOrderType(orderType);
            String detail = trimToNull(req.getItemDetailCode());
            if (detail == null) {
                detail = trimToNull(req.getItemCode());
            }
            if (detail == null) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST);
            }
            return OrderItem.createLabLine(groupCode, detail, patientId, patientName, departmentName);
        }
        String code = req.getItemCode().trim();
        String detail = trimToNull(req.getItemDetailCode());
        if (detail == null) {
            detail = code;
        }
        return OrderItem.createLabLine(code, detail, patientId, patientName, departmentName);
    }

    private static String labGroupCodeForOrderType(OrderType orderType) {
        if (orderType == null) {
            return null;
        }
        return orderType.name();
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static boolean isLegacySoapKey(long orderId, long orderItemId) {
        return orderId < 0 && orderItemId == orderId;
    }

    private static boolean isCancelledForList(String orderStatus) {
        if (orderStatus == null || orderStatus.isBlank()) {
            return false;
        }
        return "CANCELLED".equalsIgnoreCase(orderStatus.trim());
    }

    private static Instant sortInstant(Order o, OrderItem item) {
        LocalDateTime t = item.getCreatedAt() != null ? item.getCreatedAt() : o.getOrderDate();
        if (t == null) {
            return Instant.EPOCH;
        }
        return t.atZone(ZoneId.systemDefault()).toInstant();
    }

    private static Instant soapRxInstant(SoapRx rx) {
        LocalDateTime t = rx.getCreatedAt();
        if (t == null) {
            return Instant.EPOCH;
        }
        return t.atZone(ZoneId.systemDefault()).toInstant();
    }

    private static OrderResponse legacySoapOrderResponse(SoapRx rx) {
        long sid = -rx.getPrescriptionId();
        OrderItemResponse item = legacySoapItemResponse(rx);
        return new OrderResponse(
                sid,
                rx.getVisitId(),
                OrderType.PRESCRIPTION.name(),
                "REQUESTED",
                null,
                rx.getCreatedAt(),
                rx.getCreatedAt(),
                rx.getUpdatedAt(),
                List.of(item));
    }

    private static OrderItemResponse legacySoapItemResponse(SoapRx rx) {
        long sid = -rx.getPrescriptionId();
        OrderItemResponse r = new OrderItemResponse();
        r.setOrderItemId(sid);
        r.setOrderId(sid);
        r.setItemCode(null);
        String nm = OrderItemResponse.stripEncodedOrderItemSuffix(rx.getMedicationName());
        r.setItemDetailCode(nm);
        r.setItemName(nm);
        r.setDosage(rx.getDosage());
        r.setDose(null);
        r.setFrequency(rx.getFrequency());
        r.setDuration(rx.getDays());
        r.setCreatedAt(rx.getCreatedAt());
        return r;
    }
}
