package com.example.hospitalClinical.order.integration.clinicalsupport.kafka;

import com.example.hospitalClinical.common.client.external.clinicalsupport.MedicationRecordOutboundRequest;
import com.example.hospitalClinical.common.client.external.clinicalsupport.TreatmentResultOutboundRequest;
import com.example.hospitalClinical.common.client.external.clinicalsupport.TestExecutionRegisterRequest;
import com.example.hospitalClinical.common.event.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import static com.example.hospitalClinical.common.event.Event.Type.CREATE;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClinicalSupportOrderEventPublisher {

    public static final String BINDING_OUT_MEDICATION_RECORD = "output-medicationRecord-out-0";
    public static final String BINDING_OUT_TREATMENT_RESULT = "output-treatmentResult-out-0";
    public static final String BINDING_OUT_TEST_EXECUTION = "output-testExecution-out-0";

    private final StreamBridge streamBridge;
    private final ClinicalSupportOrderKafkaProperties properties;

    public void publishMedicationRecord(MedicationRecordOutboundRequest body) {
        if (!properties.isEnabled() || body == null) {
            return;
        }
        streamBridge.send(
                BINDING_OUT_MEDICATION_RECORD,
                MessageBuilder.withPayload(new Event<>(CREATE, body.getMedicationId(), body)).build());
    }

    public void publishTreatmentResult(TreatmentResultOutboundRequest body) {
        if (!properties.isEnabled() || body == null) {
            return;
        }
        streamBridge.send(
                BINDING_OUT_TREATMENT_RESULT,
                MessageBuilder.withPayload(new Event<>(CREATE, body.getProcedureResultId(), body)).build());
    }

    public void publishTestExecution(TestExecutionRegisterRequest body) {
        if (body == null) {
            return;
        }
        if (!properties.isEnabled()) {
            log.warn("[진료→진료지원][Kafka] 검사오더 발행 비활성(enabled=false) orderItemId={}", body.getOrderItemId());
            return;
        }
        boolean sent = streamBridge.send(
                BINDING_OUT_TEST_EXECUTION,
                MessageBuilder.withPayload(new Event<>(CREATE, body.getOrderItemId(), body)).build());
        if (!sent) {
            log.warn("[진료→진료지원][Kafka] 검사오더 발행 실패(streamBridge=false) orderItemId={}", body.getOrderItemId());
            return;
        }
        log.info("[진료→진료지원][Kafka] 검사오더 발행 완료 orderItemId={} executionType={}",
                body.getOrderItemId(),
                body.getExecutionType());
    }
}

