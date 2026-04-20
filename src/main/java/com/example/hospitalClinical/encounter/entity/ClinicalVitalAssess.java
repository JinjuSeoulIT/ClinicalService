package com.example.hospitalClinical.encounter.entity;

import com.example.hospitalClinical.encounter.dto.ClinicalVitalAssessSaveRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "CLINICAL_VITAL_ASSESS",
        uniqueConstraints = @UniqueConstraint(name = "UK_CLINICAL_VITAL_ASSESS_VISIT", columnNames = "VISIT_ID"))
public class ClinicalVitalAssess {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "clinical_vital_assess_seq_gen")
    @SequenceGenerator(
            name = "clinical_vital_assess_seq_gen",
            sequenceName = "CLINICAL_VITAL_ASSESS_SEQ",
            allocationSize = 1)
    @Column(name = "VITAL_ASSESS_ID", nullable = false)
    private Long vitalAssessId;

    @Column(name = "VISIT_ID", nullable = false)
    private Long visitId;

    @Column(name = "RECEPTION_ID")
    private Long receptionId;

    @Column(name = "RECORDED_AT")
    private LocalDateTime recordedAt;

    @Column(name = "SYSTOLIC_BP")
    private Integer systolicBp;

    @Column(name = "DIASTOLIC_BP")
    private Integer diastolicBp;

    @Column(name = "PULSE")
    private Integer pulse;

    @Column(name = "RESPIRATION")
    private Integer respiration;

    @Column(name = "TEMPERATURE", precision = 4, scale = 1)
    private BigDecimal temperature;

    @Column(name = "SPO2")
    private Integer spo2;

    @Column(name = "PAIN_SCORE")
    private Integer painScore;

    @Column(name = "CONSCIOUSNESS_LEVEL", length = 30)
    private String consciousnessLevel;

    @Column(name = "HEIGHT_CM", length = 5)
    private String heightCm;

    @Column(name = "WEIGHT_KG", length = 5)
    private String weightKg;

    @Column(name = "CHIEF_COMPLAINT", length = 500)
    private String chiefComplaint;

    @Column(name = "VISIT_REASON", length = 500)
    private String visitReason;

    @Column(name = "HISTORY_PRESENT_ILLNESS", length = 1000)
    private String historyPresentIllness;

    @Column(name = "PAST_HISTORY", length = 500)
    private String pastHistory;

    @Column(name = "FAMILY_HISTORY", length = 500)
    private String familyHistory;

    @Column(name = "ALLERGY", length = 500)
    private String allergy;

    @Column(name = "CURRENT_MEDICATION", length = 500)
    private String currentMedication;

    @Column(name = "STATUS", length = 20)
    private String status;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    protected ClinicalVitalAssess() {}

    public static ClinicalVitalAssess createNew(Long visitId, Long receptionId) {
        if (visitId == null) {
            throw new IllegalArgumentException("visitId는 필수입니다.");
        }
        ClinicalVitalAssess e = new ClinicalVitalAssess();
        e.visitId = visitId;
        e.receptionId = receptionId;
        e.status = "ACTIVE";
        return e;
    }

    public void applySave(ClinicalVitalAssessSaveRequest req) {
        if (req.getReceptionId() != null) {
            this.receptionId = req.getReceptionId();
        }
        LocalDateTime reqRecorded = req.getRecordedAt();
        if (reqRecorded != null) {
            this.recordedAt = reqRecorded;
        } else if (this.recordedAt == null) {
            this.recordedAt = LocalDateTime.now();
        }
        this.systolicBp = req.getSystolicBp();
        this.diastolicBp = req.getDiastolicBp();
        this.pulse = req.getPulse();
        this.respiration = req.getRespiration();
        this.temperature = req.getTemperature();
        if (req.getSpo2() != null) {
            this.spo2 = req.getSpo2();
        }
        if (req.getPainScore() != null) {
            this.painScore = req.getPainScore();
        }
        if (req.getConsciousnessLevel() != null) {
            this.consciousnessLevel = trimToNull(req.getConsciousnessLevel());
        }
        if (req.getHeightCm() != null) {
            String hc = trimToNull(req.getHeightCm());
            if (hc != null) {
                this.heightCm = hc;
            }
        }
        if (req.getWeightKg() != null) {
            String wk = trimToNull(req.getWeightKg());
            if (wk != null) {
                this.weightKg = wk;
            }
        }
        this.chiefComplaint = trimToNull(req.getChiefComplaint());
        this.visitReason = trimToNull(req.getVisitReason());
        this.historyPresentIllness = trimToNull(req.getHistoryPresentIllness());
        this.pastHistory = trimToNull(req.getPastHistory());
        this.familyHistory = trimToNull(req.getFamilyHistory());
        this.allergy = trimToNull(req.getAllergy());
        this.currentMedication = trimToNull(req.getCurrentMedication());
        if (req.getStatus() != null && !req.getStatus().isBlank()) {
            this.status = req.getStatus().trim();
        } else if (this.status == null || this.status.isBlank()) {
            this.status = "ACTIVE";
        }
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (status == null || status.isBlank()) {
            status = "ACTIVE";
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getVitalAssessId() {
        return vitalAssessId;
    }

    public Long getVisitId() {
        return visitId;
    }

    public Long getReceptionId() {
        return receptionId;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public Integer getSystolicBp() {
        return systolicBp;
    }

    public Integer getDiastolicBp() {
        return diastolicBp;
    }

    public Integer getPulse() {
        return pulse;
    }

    public Integer getRespiration() {
        return respiration;
    }

    public BigDecimal getTemperature() {
        return temperature;
    }

    public Integer getSpo2() {
        return spo2;
    }

    public Integer getPainScore() {
        return painScore;
    }

    public String getConsciousnessLevel() {
        return consciousnessLevel;
    }

    public String getHeightCm() {
        return heightCm;
    }

    public String getWeightKg() {
        return weightKg;
    }

    public String getChiefComplaint() {
        return chiefComplaint;
    }

    public String getVisitReason() {
        return visitReason;
    }

    public String getHistoryPresentIllness() {
        return historyPresentIllness;
    }

    public String getPastHistory() {
        return pastHistory;
    }

    public String getFamilyHistory() {
        return familyHistory;
    }

    public String getAllergy() {
        return allergy;
    }

    public String getCurrentMedication() {
        return currentMedication;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
