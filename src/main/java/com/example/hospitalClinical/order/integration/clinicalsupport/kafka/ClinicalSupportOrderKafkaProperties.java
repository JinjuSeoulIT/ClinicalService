package com.example.hospitalClinical.order.integration.clinicalsupport.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "clinical-support.order")
public class ClinicalSupportOrderKafkaProperties {
    private boolean enabled = true;
}

