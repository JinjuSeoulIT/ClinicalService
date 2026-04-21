package com.example.hospitalClinical.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "CLINICAL_TREATMENT_ORDER_ITEM")
public class TreatmentResult {

    @Id
    @Column(name = "PROCEDURE_RESULT_ID", length = 64, nullable = false)
    private String procedureResultId;

    @Column(name = "PATIENT_ID", nullable = false)
    private Long patientId;

    @Column(name = "PATIENT_NAME", length = 200)
    private String patientName;

    @Column(name = "DEPARTMENT_NAME", length = 200)
    private String departmentName;

    @Column(name = "STATUS", length = 50)
    private String status;

    @Column(name = "DETAIL", length = 1000)
    private String detail;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    protected TreatmentResult() {}

    public static TreatmentResult create(
            String procedureResultId,
            Long patientId,
            String patientName,
            String departmentName,
            String status,
            String detail) {
        TreatmentResult t = new TreatmentResult();
        t.procedureResultId = procedureResultId;
        t.patientId = patientId;
        t.patientName = patientName;
        t.departmentName = departmentName;
        t.status = status != null && !status.isBlank() ? status.trim() : "REQUESTED";
        t.detail = detail;
        return t;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
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

    public void setStatus(String status) {
        this.status = status != null && !status.isBlank() ? status.trim() : "REQUESTED";
    }

    public String getDetail() {
        return detail;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
