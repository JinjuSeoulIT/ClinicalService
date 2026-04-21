package com.example.hospitalClinical.order.integration.clinicalsupport.inbound;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.support-feedback.kafka", name = "enabled", havingValue = "true")
public class SupportOrderFeedbackKafkaListener {

    private final SupportOrderFeedbackProcessor processor;

    @KafkaListener(
            topics = "${app.support-feedback.kafka.topic-medication}",
            containerFactory = "supportFeedbackKafkaListenerContainerFactory")
    public void onMedication(String payload) {
        processor.onMedicationMessage(payload);
    }

    @KafkaListener(
            topics = "${app.support-feedback.kafka.topic-treatment}",
            containerFactory = "supportFeedbackKafkaListenerContainerFactory")
    public void onTreatment(String payload) {
        processor.onTreatmentMessage(payload);
    }

    @KafkaListener(
            topics = "${app.support-feedback.kafka.topic-test-execution}",
            containerFactory = "supportFeedbackKafkaListenerContainerFactory")
    public void onTestExecution(String payload) {
        processor.onTestExecutionMessage(payload);
    }
}
