package com.example.hospitalClinical.common.client.internal.reception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReceptionResponse {

    private Long receptionId;
    private String receptionNo;
    private Long patientId;
    private String patientName;
    private String visitType;
    private String departmentId;
    private String departmentName;
    private String doctorId;
    private String doctorName;
    private Long reservationId;
    private LocalDateTime scheduledAt;
    private LocalDateTime arrivedAt;
    private String status;
    private String note;
    private Boolean isActive;
    private LocalDateTime inactiveAt;
    private String inactiveReasonCode;
    private String inactiveReasonText;
    private String cancelReasonCode;
    private String cancelReasonText;
    private String holdReasonCode;
    private String holdReasonText;
    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getReceptionId() { return receptionId; }
    public void setReceptionId(Long receptionId) { this.receptionId = receptionId; }
    public String getReceptionNo() { return receptionNo; }
    public void setReceptionNo(String receptionNo) { this.receptionNo = receptionNo; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getVisitType() { return visitType; }
    public void setVisitType(String visitType) { this.visitType = visitType; }
    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
    public LocalDateTime getArrivedAt() { return arrivedAt; }
    public void setArrivedAt(LocalDateTime arrivedAt) { this.arrivedAt = arrivedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public LocalDateTime getInactiveAt() { return inactiveAt; }
    public void setInactiveAt(LocalDateTime inactiveAt) { this.inactiveAt = inactiveAt; }
    public String getInactiveReasonCode() { return inactiveReasonCode; }
    public void setInactiveReasonCode(String inactiveReasonCode) { this.inactiveReasonCode = inactiveReasonCode; }
    public String getInactiveReasonText() { return inactiveReasonText; }
    public void setInactiveReasonText(String inactiveReasonText) { this.inactiveReasonText = inactiveReasonText; }
    public String getCancelReasonCode() { return cancelReasonCode; }
    public void setCancelReasonCode(String cancelReasonCode) { this.cancelReasonCode = cancelReasonCode; }
    public String getCancelReasonText() { return cancelReasonText; }
    public void setCancelReasonText(String cancelReasonText) { this.cancelReasonText = cancelReasonText; }
    public String getHoldReasonCode() { return holdReasonCode; }
    public void setHoldReasonCode(String holdReasonCode) { this.holdReasonCode = holdReasonCode; }
    public String getHoldReasonText() { return holdReasonText; }
    public void setHoldReasonText(String holdReasonText) { this.holdReasonText = holdReasonText; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public Long getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
