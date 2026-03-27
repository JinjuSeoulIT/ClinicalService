package com.example.hospitalClinical.encounter.dto;

import jakarta.validation.constraints.NotNull;

public class VisitStartRequest {

    @NotNull(message = "receptionId는 필수입니다.")
    private Long receptionId;

    private Long changedBy;

    public Long getReceptionId() { return receptionId; }
    public void setReceptionId(Long receptionId) { this.receptionId = receptionId; }
    public Long getChangedBy() { return changedBy; }
    public void setChangedBy(Long changedBy) { this.changedBy = changedBy; }
}
