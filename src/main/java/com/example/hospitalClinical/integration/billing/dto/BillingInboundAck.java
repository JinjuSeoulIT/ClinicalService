package com.example.hospitalClinical.integration.billing.dto;

public record BillingInboundAck(
        String eventType,
        Long receptionId,
        Long visitId,
        String note,
        String receivedAtIso
) {
}
