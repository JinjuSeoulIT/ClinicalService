package com.example.hospitalClinical.order.dto;

import jakarta.validation.constraints.NotNull;

public class MedicationRecordCreateRequest {

    @NotNull(message = "doseNumber는 필수입니다.")
    private Double doseNumber;

    @NotNull(message = "doseUnit는 필수입니다.")
    private String doseUnit;

    private String doseKind;

    private String status;

    private String patientName;

    private String departmentName;

    public Double getDoseNumber() {
        return doseNumber;
    }

    public void setDoseNumber(Double doseNumber) {
        this.doseNumber = doseNumber;
    }

    public String getDoseUnit() {
        return doseUnit;
    }

    public void setDoseUnit(String doseUnit) {
        this.doseUnit = doseUnit;
    }

    public String getDoseKind() {
        return doseKind;
    }

    public void setDoseKind(String doseKind) {
        this.doseKind = doseKind;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
}
