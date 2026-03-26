package com.example.hospitalClinical.common.client.external.drug;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "drug.api")
public class DrugApiProperties {

    private String baseUrl = "";
    private String serviceKey = "";
}
