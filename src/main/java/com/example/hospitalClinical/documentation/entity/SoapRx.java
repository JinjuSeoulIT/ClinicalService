package com.example.hospitalClinical.documentation.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "VISIT_SOAP_PRESCRIPTION")
public class SoapRx {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "visit_soap_rx_seq_gen")
    @SequenceGenerator(name = "visit_soap_rx_seq_gen", sequenceName = "CL_VSOAP_RX_SEQ", allocationSize = 1)
    @Column(name = "PRESCRIPTION_ID", nullable = false)
    private Long prescriptionId;

    @Column(name = "VISIT_ID", nullable = false)
    private Long visitId;

    @Column(name = "MEDICATION_NAME", length = 600)
    private String medicationName;

    @Column(name = "DOSAGE", length = 200)
    private String dosage;

    @Column(name = "FREQUENCY", length = 100)
    private String frequency;

    @Column(name = "DAYS", length = 50)
    private String days;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    protected SoapRx() {}

    public static SoapRx create(
            Long visitId, String medicationName, String dosage, String frequency, String days) {
        SoapRx p = new SoapRx();
        p.visitId = visitId;
        p.medicationName = medicationName;
        p.dosage = dosage;
        p.frequency = frequency;
        p.days = days;
        return p;
    }

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getPrescriptionId() {
        return prescriptionId;
    }

    public Long getVisitId() {
        return visitId;
    }

    public String getMedicationName() {
        return medicationName;
    }

    public String getDosage() {
        return dosage;
    }

    public String getDays() {
        return days;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
