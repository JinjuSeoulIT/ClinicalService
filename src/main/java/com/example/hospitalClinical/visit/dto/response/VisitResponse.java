package com.example.hospitalClinical.visit.dto.response;

import com.example.hospitalClinical.visit.entity.Visit;
import com.example.hospitalClinical.visit.entity.VisitStatus;
import com.example.hospitalClinical.visit.entity.VisitType;

import java.time.LocalDateTime;

public class VisitResponse {
    private Long visitId;
    private Long patientId;
    private VisitType visitType;
    private VisitStatus visitStatus;
    private LocalDateTime visitAt;
    private Long deptId;
    private Long doctorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static VisitResponse from(Visit v) {
        VisitResponse r = new VisitResponse();
        r.visitId = v.getVisitId();
        r.patientId = v.getPatientId();
        r.visitType = v.getVisitType();
        r.visitStatus = v.getVisitStatus();
        r.visitAt = v.getVisitAt();
        r.deptId = v.getDeptId();
        r.doctorId = v.getDoctorId();
        r.createdAt = v.getCreatedAt();
        r.updatedAt = v.getUpdatedAt();
        return r;
    }

    public Long getVisitId() { return visitId; }
    public Long getPatientId() { return patientId; }
    public VisitType getVisitType() { return visitType; }
    public VisitStatus getVisitStatus() { return visitStatus; }
    public LocalDateTime getVisitAt() { return visitAt; }
    public Long getDeptId() { return deptId; }
    public Long getDoctorId() { return doctorId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}