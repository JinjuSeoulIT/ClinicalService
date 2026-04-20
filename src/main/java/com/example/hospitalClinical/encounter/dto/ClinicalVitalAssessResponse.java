package com.example.hospitalClinical.encounter.dto;

import com.example.hospitalClinical.encounter.entity.ClinicalVitalAssess;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ClinicalVitalAssessResponse {

    private Long vitalAssessId;
    private Long visitId;
    private Long receptionId;
    private LocalDateTime recordedAt;
    private Integer systolicBp;
    private Integer diastolicBp;
    private Integer pulse;
    private Integer respiration;
    private BigDecimal temperature;
    private Integer spo2;
    private Integer painScore;
    private String consciousnessLevel;
    private String heightCm;
    private String weightKg;
    private String chiefComplaint;
    private String visitReason;
    private String historyPresentIllness;
    private String pastHistory;
    private String familyHistory;
    private String allergy;
    private String currentMedication;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<VitalAssessSaveHistoryLine> chartSaveHistory = new ArrayList<>();

    public static ClinicalVitalAssessResponse from(ClinicalVitalAssess e) {
        return from(e, List.of());
    }

    public static ClinicalVitalAssessResponse from(ClinicalVitalAssess e, List<VitalAssessSaveHistoryLine> chartSaveHistory) {
        ClinicalVitalAssessResponse r = new ClinicalVitalAssessResponse();
        r.vitalAssessId = e.getVitalAssessId();
        r.visitId = e.getVisitId();
        r.receptionId = e.getReceptionId();
        r.recordedAt = e.getRecordedAt();
        r.systolicBp = e.getSystolicBp();
        r.diastolicBp = e.getDiastolicBp();
        r.pulse = e.getPulse();
        r.respiration = e.getRespiration();
        r.temperature = e.getTemperature();
        r.spo2 = e.getSpo2();
        r.painScore = e.getPainScore();
        r.consciousnessLevel = e.getConsciousnessLevel();
        r.heightCm = e.getHeightCm();
        r.weightKg = e.getWeightKg();
        r.chiefComplaint = e.getChiefComplaint();
        r.visitReason = e.getVisitReason();
        r.historyPresentIllness = e.getHistoryPresentIllness();
        r.pastHistory = e.getPastHistory();
        r.familyHistory = e.getFamilyHistory();
        r.allergy = e.getAllergy();
        r.currentMedication = e.getCurrentMedication();
        r.status = e.getStatus();
        r.createdAt = e.getCreatedAt();
        r.updatedAt = e.getUpdatedAt();
        r.chartSaveHistory = chartSaveHistory != null ? chartSaveHistory : new ArrayList<>();
        return r;
    }
}
