package com.example.hospitalClinical.integration.billing.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class BillingInboundNotificationRequest {

    @NotBlank
    private String eventType;

    private Long receptionId;
    private Long visitId;
    private String note;
    private Map<String, Object> payload;
}
