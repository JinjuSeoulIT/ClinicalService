package com.example.hospitalClinical.encounter.dto;

import java.time.LocalDateTime;

public class VisitCreateRequest {

    private Long patientId;
    private Long doctorId;
    private Long receptionId;
    private String visitStatus;
    private LocalDateTime startTime;

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public Long getReceptionId() { return receptionId; }
    public void setReceptionId(Long receptionId) { this.receptionId = receptionId; }
    public String getVisitStatus() { return visitStatus; }
    public void setVisitStatus(String visitStatus) { this.visitStatus = visitStatus; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
}
