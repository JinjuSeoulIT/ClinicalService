package com.example.hospitalClinical.encounter.integration.testresult;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.test-result-ready.kafka")
public class TestResultReadyKafkaProperties {
    private boolean enabled;
    private String topic = "test-result-ready";
    private String groupId = "hospital-clinical-test-result-ready";
    private String bootstrapServers = "192.168.1.60:9092";
}
