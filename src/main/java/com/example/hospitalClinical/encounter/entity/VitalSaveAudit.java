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
public class VitalSaveAudit {

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

    @Column(name = "CHANGE_SUMMARY", length = 4000)
    private String changeSummary;

    protected VitalSaveAudit() {}

    public static VitalSaveAudit create(Long visitId, LocalDateTime recordedAt, String changeSummary) {
        VitalSaveAudit a = new VitalSaveAudit();
        a.visitId = visitId;
        a.recordedAt = recordedAt;
        a.changeSummary = changeSummary != null && !changeSummary.isBlank() ? changeSummary : null;
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

    public String getChangeSummary() {
        return changeSummary;
    }
}
