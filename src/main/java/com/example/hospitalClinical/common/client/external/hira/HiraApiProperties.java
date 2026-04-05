package com.example.hospitalClinical.common.client.external.hira;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "hira.api")
public class HiraApiProperties {

    private String baseUrl;
    private String serviceKey;
}