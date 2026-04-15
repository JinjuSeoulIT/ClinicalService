package com.example.hospitalClinical.order.dto;

import com.example.hospitalClinical.order.entity.MedicationRecord;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MedicationRecordResponse {

    private String medicationId;
    private Long patientId;
    private String patientName;
    private String departmentName;
    private BigDecimal doseNumber;
    private String doseUnit;
    private String doseKind;
    private String status;
    private LocalDateTime createdAt;

    public static MedicationRecordResponse from(MedicationRecord e) {
        MedicationRecordResponse r = new MedicationRecordResponse();
        r.medicationId = e.getMedicationId();
        r.patientId = e.getPatientId();
        r.patientName = e.getPatientName();
        r.departmentName = e.getDepartmentName();
        r.doseNumber = e.getDoseNumber();
        r.doseUnit = e.getDoseUnit();
        r.doseKind = e.getDoseKind();
        r.status = e.getStatus();
        r.createdAt = e.getCreatedAt();
        return r;
    }

    public String getMedicationId() {
        return medicationId;
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

    public BigDecimal getDoseNumber() {
        return doseNumber;
    }

    public String getDoseUnit() {
        return doseUnit;
    }

    public String getDoseKind() {
        return doseKind;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
