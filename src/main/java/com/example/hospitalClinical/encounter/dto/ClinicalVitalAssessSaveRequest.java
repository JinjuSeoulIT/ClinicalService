package com.example.hospitalClinical.encounter.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ClinicalVitalAssessSaveRequest {

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
}
