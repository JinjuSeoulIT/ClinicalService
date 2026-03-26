package com.example.hospitalClinical.order.integration;

import com.example.hospitalClinical.common.client.external.clinicalsupport.ClinicalSupportApiProperties;
import com.example.hospitalClinical.common.client.external.clinicalsupport.TestExecutionApiClient;
import com.example.hospitalClinical.common.client.external.clinicalsupport.TestExecutionRegisterRequest;
import com.example.hospitalClinical.order.event.LabOrderCommittedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class TestExecutionRegistrationListener {

    private final ClinicalSupportApiProperties clinicalSupportApiProperties;
    private final TestExecutionApiClient testExecutionApiClient;

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
            TestExecutionRegisterRequest req = TestExecutionRegisterRequest.builder()
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

    private static String executionTypeFor(String orderType) {
        if (orderType == null) {
            return "SPECIMEN";
        }
        return switch (orderType) {
            case "IMAGING" -> "IMAGING";
            case "PROCEDURE" -> "PROCEDURE";
            default -> "SPECIMEN";
        };
    }
}
