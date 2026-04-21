package com.example.hospitalClinical.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "CLINICAL_MEDICATION_ORDER_ITEM")
public class MedicationRecord {

    @Id
    @Column(name = "MEDICATION_ID", length = 64, nullable = false)
    private String medicationId;

    @Column(name = "PATIENT_ID", nullable = false)
    private Long patientId;

    @Column(name = "PATIENT_NAME", length = 200)
    private String patientName;

    @Column(name = "DEPARTMENT_NAME", length = 200)
    private String departmentName;

    @Column(name = "DOSE_NUMBER", precision = 14, scale = 4)
    private BigDecimal doseNumber;

    @Column(name = "DOSE_UNIT", length = 50)
    private String doseUnit;

    @Column(name = "DOSE_KIND", length = 100)
    private String doseKind;

    @Column(name = "STATUS", length = 50)
    private String status;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    protected MedicationRecord() {}

    public static MedicationRecord create(
            String medicationId,
            Long patientId,
            String patientName,
            String departmentName,
            BigDecimal doseNumber,
            String doseUnit,
            String doseKind,
            String status) {
        MedicationRecord m = new MedicationRecord();
        m.medicationId = medicationId;
        m.patientId = patientId;
        m.patientName = patientName;
        m.departmentName = departmentName;
        m.doseNumber = doseNumber;
        m.doseUnit = doseUnit;
        m.doseKind = doseKind != null && !doseKind.isBlank() ? doseKind.trim() : null;
        m.status = status != null && !status.isBlank() ? status.trim() : "REQUESTED";
        return m;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
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

    public void setStatus(String status) {
        this.status = status != null && !status.isBlank() ? status.trim() : "REQUESTED";
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
