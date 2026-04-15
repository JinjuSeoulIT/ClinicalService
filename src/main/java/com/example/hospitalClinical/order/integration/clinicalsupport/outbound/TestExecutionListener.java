package com.example.hospitalClinical.order.integration.clinicalsupport.outbound;

import com.example.hospitalClinical.common.client.external.clinicalsupport.ClinicalSupportApiProperties;
import com.example.hospitalClinical.common.client.external.clinicalsupport.TestExecutionApiClient;
import com.example.hospitalClinical.common.client.external.clinicalsupport.TestExecutionRegisterRequest;
import com.example.hospitalClinical.common.client.internal.reception.ReceptionClient;
import com.example.hospitalClinical.common.client.internal.reception.ReceptionResponse;
import com.example.hospitalClinical.encounter.entity.Visit;
import com.example.hospitalClinical.encounter.repository.VisitRepo;
import com.example.hospitalClinical.order.entity.Order;
import com.example.hospitalClinical.order.entity.OrderItem;
import com.example.hospitalClinical.order.entity.OrderType;
import com.example.hospitalClinical.order.event.LabOrderCommittedEvent;
import com.example.hospitalClinical.order.repository.OrderItemRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class TestExecutionListener {

    private final ClinicalSupportApiProperties clinicalSupportApiProperties;
    private final TestExecutionApiClient testExecutionApiClient;
    private final OrderItemRepo orderItemRepo;
    private final VisitRepo visitRepo;
    private final ReceptionClient receptionClient;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLabOrderCommitted(LabOrderCommittedEvent event) {
        if (!clinicalSupportApiProperties.isEnabled()) {
            return;
        }
        if (event.orderItemIds() == null || event.orderItemIds().isEmpty()) {
            return;
        }
        String executionType = executionTypeFor(event.orderType());
        for (Long orderItemId : event.orderItemIds()) {
            if (orderItemId == null) {
                continue;
            }
            Long patientId = null;
            String patientName = null;
            String departmentName = null;
            Optional<OrderItem> itemOpt = orderItemRepo.findWithOrderByOrderItemId(orderItemId);
            if (itemOpt.isEmpty()) {
                log.warn("검사수행 연동: OrderItem 없음 orderItemId={}", orderItemId);
            } else {
                Order order = itemOpt.get().getOrder();
                Long visitId = order != null ? order.getVisitId() : null;
                if (visitId != null) {
                    Optional<Visit> visitOpt = visitRepo.findById(visitId);
                    if (visitOpt.isPresent()) {
                        Visit visit = visitOpt.get();
                        patientId = visit.getPatientId();
                        try {
                            ReceptionResponse r = receptionClient.getReception(visit.getReceptionId());
                            patientName = r.getPatientName();
                            departmentName = r.getDepartmentName();
                        } catch (Exception e) {
                            log.warn(
                                    "접수 조회 실패(검사수행 연동) visitId={} receptionId={}",
                                    visitId,
                                    visit.getReceptionId(),
                                    e);
                        }
                    }
                }
            }
            TestExecutionRegisterRequest req = TestExecutionRegisterRequest.builder()
                    .patientId(patientId)
                    .patientName(patientName)
                    .departmentName(departmentName)
                    .orderItemId(orderItemId)
                    .executionType(executionType)
                    .progressStatus("WAITING")
                    .retryNo(0)
                    .performerId(event.doctorId())
                    .build();
            try {
                testExecutionApiClient.register(req);
            } catch (Exception e) {
                log.warn(
                        "검사수행 연동 실패(오더는 유지됨) orderItemId={} message={}",
                        orderItemId,
                        e.getMessage());
            }
        }
    }

    private static String executionTypeFor(OrderType orderType) {
        if (orderType == null) {
            return "SPECIMEN";
        }
        return switch (orderType) {
            case IMAGING -> "IMAGING";
            case PATHOLOGY -> "PATHOLOGY";
            case ENDOSCOPY -> "ENDOSCOPY";
            case PHYSIOLOGICAL -> "PHYSIOLOGICAL";
            default -> "SPECIMEN";
        };
    }
}
