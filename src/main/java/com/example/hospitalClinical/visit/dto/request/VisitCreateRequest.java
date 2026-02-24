package com.example.hospitalClinical.visit.dto.request;

import com.example.hospitalClinical.visit.entity.VisitType;
import java.time.LocalDateTime;

public class VisitCreateRequest {
    private Long patientId;
    private VisitType visitType;      // OUT/IN/ER
    private LocalDateTime visitAt;    // 없으면 now로 처리
    private Long deptId;
    private Long doctorId;

    public Long getPatientId() { return patientId; }
    public VisitType getVisitType() { return visitType; }
    public LocalDateTime getVisitAt() { return visitAt; }
    public Long getDeptId() { return deptId; }
    public Long getDoctorId() { return doctorId; }
}