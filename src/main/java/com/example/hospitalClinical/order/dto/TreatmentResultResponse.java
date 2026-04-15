package com.example.hospitalClinical.order.dto;

import com.example.hospitalClinical.order.entity.TreatmentResult;

import java.time.LocalDateTime;

public class TreatmentResultResponse {

    private String procedureResultId;
    private Long patientId;
    private String patientName;
    private String departmentName;
    private String status;
    private String detail;
    private LocalDateTime createdAt;

    public static TreatmentResultResponse from(TreatmentResult e) {
        TreatmentResultResponse r = new TreatmentResultResponse();
        r.procedureResultId = e.getProcedureResultId();
        r.patientId = e.getPatientId();
        r.patientName = e.getPatientName();
        r.departmentName = e.getDepartmentName();
        r.status = e.getStatus();
        r.detail = e.getDetail();
        r.createdAt = e.getCreatedAt();
        return r;
    }

    public String getProcedureResultId() {
        return procedureResultId;
    }

    public Long getPatientId() {
        return patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public String getStatus() {
        return status;
    }

    public String getDetail() {
        return detail;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
