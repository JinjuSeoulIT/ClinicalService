package com.example.hospitalClinical.encounter.dto;

import com.example.hospitalClinical.encounter.entity.Visit;

import java.time.LocalDateTime;

public class VisitStartResponse {

    private Long visitId;
    private Long patientId;
    private String doctorId;
    private Long receptionId;
    private String visitStatus;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static VisitStartResponse from(Visit v) {
        VisitStartResponse r = new VisitStartResponse();
        r.visitId = v.getVisitId();
        r.patientId = v.getPatientId();
        r.doctorId = v.getDoctorId();
        r.receptionId = v.getReceptionId();
        r.visitStatus = v.getVisitStatus();
        r.startTime = v.getStartTime();
        r.endTime = v.getEndTime();
        r.createdAt = v.getCreatedAt();
        r.updatedAt = v.getUpdatedAt();
        return r;
    }

    public Long getVisitId() { return visitId; }
    public Long getPatientId() { return patientId; }
    public String getDoctorId() { return doctorId; }
    public Long getReceptionId() { return receptionId; }
    public String getVisitStatus() { return visitStatus; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
