package com.example.hospitalClinical.documentation.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "CLINICAL_DIAGNOSIS")
public class Diagnosis {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "diagnosis_seq_gen")
    @SequenceGenerator(name = "diagnosis_seq_gen", sequenceName = "CL_DIAGNOSIS_SEQ", allocationSize = 1)
    @Column(name = "DIAGNOSIS_ID", nullable = false)
    private Long diagnosisId;

    @Column(name = "NOTE_ID", nullable = false)
    private Long noteId;

    @Column(name = "PATIENT_CODE", length = 50)
    private String patientCode;

    @Column(name = "DIAGNOSIS_CODE", length = 50)
    private String diagnosisCode;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    protected Diagnosis() {}

    public static Diagnosis create(Long noteId, String patientCode, String diagnosisCode, String description) {
        Diagnosis d = new Diagnosis();
        d.noteId = noteId;
        d.patientCode = patientCode;
        d.diagnosisCode = diagnosisCode;
        d.description = description;
        return d;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public Long getDiagnosisId() { return diagnosisId; }
    public Long getNoteId() { return noteId; }
    public String getPatientCode() { return patientCode; }
    public void setPatientCode(String patientCode) { this.patientCode = patientCode; }
    public String getDiagnosisCode() { return diagnosisCode; }
    public void setDiagnosisCode(String diagnosisCode) { this.diagnosisCode = diagnosisCode; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
