package com.example.hospitalClinical.order.integration.clinicalsupport.inbound;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.support-feedback.kafka")
public class SupportFeedbackKafkaProperties {
    private boolean enabled;
    private String bootstrapServers = "192.168.1.60:9092";
    private String groupId = "hospital-clinical-support-feedback";
    private String topicMedication = "medicationRecord";
    private String topicTreatment = "treatmentResult";
    private String topicTestExecution = "testExecution";
}
