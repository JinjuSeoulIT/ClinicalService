package com.example.hospitalClinical.common.client.internal.reception;

public class ReceptionStatusUpdateRequest {

    private String status;
    private Long changedBy;
    private String reasonCode;
    private String reasonText;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getChangedBy() { return changedBy; }
    public void setChangedBy(Long changedBy) { this.changedBy = changedBy; }
    public String getReasonCode() { return reasonCode; }
    public void setReasonCode(String reasonCode) { this.reasonCode = reasonCode; }
    public String getReasonText() { return reasonText; }
    public void setReasonText(String reasonText) { this.reasonText = reasonText; }
}
