package com.example.hospitalClinical.common.client.external.billing;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "billing.api")
public class BillingApiProperties {

    private boolean enabled = true;

    private String baseUrl = "http://192.168.1.68:8081";
}
