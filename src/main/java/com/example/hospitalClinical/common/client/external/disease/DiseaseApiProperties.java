package com.example.hospitalClinical.common.client.external.disease;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "disease.api")
public class DiseaseApiProperties {

    private String baseUrl = "";
    private String serviceKey = "";
}
