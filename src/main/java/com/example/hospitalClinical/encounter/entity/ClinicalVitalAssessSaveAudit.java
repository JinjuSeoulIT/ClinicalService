package com.example.hospitalClinical.encounter.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "CLINICAL_VITAL_SAVE_AUDIT")
public class ClinicalVitalAssessSaveAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "clinical_vital_save_audit_seq")
    @SequenceGenerator(
            name = "clinical_vital_save_audit_seq",
            sequenceName = "CLINICAL_VITAL_SAVE_AUDIT_SEQ",
            allocationSize = 1)
    @Column(name = "SAVE_AUDIT_ID", nullable = false)
    private Long saveAuditId;

    @Column(name = "VISIT_ID", nullable = false)
    private Long visitId;

    @Column(name = "RECORDED_AT")
    private LocalDateTime recordedAt;

    @Column(name = "SAVED_AT", nullable = false)
    private LocalDateTime savedAt;

    protected ClinicalVitalAssessSaveAudit() {}

    public static ClinicalVitalAssessSaveAudit create(Long visitId, LocalDateTime recordedAt) {
        ClinicalVitalAssessSaveAudit a = new ClinicalVitalAssessSaveAudit();
        a.visitId = visitId;
        a.recordedAt = recordedAt;
        return a;
    }

    @PrePersist
    void prePersist() {
        if (savedAt == null) {
            savedAt = LocalDateTime.now();
        }
    }

    public Long getSaveAuditId() {
        return saveAuditId;
    }

    public Long getVisitId() {
        return visitId;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public LocalDateTime getSavedAt() {
        return savedAt;
    }
}
