package com.example.hospitalClinical.encounter.service;

import com.example.hospitalClinical.encounter.dto.ClinicalVitalAssessResponse;
import com.example.hospitalClinical.encounter.dto.ClinicalVitalAssessSaveRequest;

import java.util.Optional;

public interface ClinicalVitalAssessService {

    Optional<ClinicalVitalAssessResponse> getByVisitId(Long visitId);

    ClinicalVitalAssessResponse upsert(Long visitId, ClinicalVitalAssessSaveRequest request);
}
