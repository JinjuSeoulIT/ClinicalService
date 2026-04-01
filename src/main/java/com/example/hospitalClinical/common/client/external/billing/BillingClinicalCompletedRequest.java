package com.example.hospitalClinical.common.client.external.billing;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class BillingClinicalCompletedRequest {
    private String eventId;
    private Long visitId;
    private Long patientId;
    private String status;
    private LocalDateTime occurredAt;
    private List<BillingClinicalClaimItem> items;
}
