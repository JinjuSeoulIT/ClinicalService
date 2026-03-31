package com.hospital.billing.dto.integration;

import java.time.LocalDateTime;

public class ClinicalCompletedRequest {

    private String eventId;
    private Long visitId;
    private Long patientId;
    private String status;
    private LocalDateTime occurredAt;

    public ClinicalCompletedRequest() {
    }

    public ClinicalCompletedRequest(String eventId, Long visitId, Long patientId, String status, LocalDateTime occurredAt) {
        this.eventId = eventId;
        this.visitId = visitId;
        this.patientId = patientId;
        this.status = status;
        this.occurredAt = occurredAt;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Long getVisitId() {
        return visitId;
    }

    public void setVisitId(Long visitId) {
        this.visitId = visitId;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }
}