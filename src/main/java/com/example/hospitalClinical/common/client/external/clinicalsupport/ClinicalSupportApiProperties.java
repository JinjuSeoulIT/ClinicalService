package com.example.hospitalClinical.common.client.external.clinicalsupport;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "clinical-support.api")
public class ClinicalSupportApiProperties {

    private boolean enabled = true;

    private String baseUrl = "http://192.168.1.66:8181";
}
