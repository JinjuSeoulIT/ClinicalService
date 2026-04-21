package com.example.hospitalClinical.encounter.integration.testresult;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.test-result-ready.kafka", name = "enabled", havingValue = "true")
public class TestResultReadyKafkaListener {

    private final TestResultReadyMessageParser parser;
    private final TestResultReadyStore store;

    @KafkaListener(
            topics = "${app.test-result-ready.kafka.topic}",
            containerFactory = "testResultReadyKafkaListenerContainerFactory")
    public void onMessage(String payload) {
        parser.parse(payload).ifPresent(store::put);
    }
}
