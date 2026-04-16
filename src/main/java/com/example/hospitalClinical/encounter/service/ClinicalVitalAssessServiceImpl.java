package com.example.hospitalClinical.encounter.service;

import com.example.hospitalClinical.common.exception.BusinessException;
import com.example.hospitalClinical.common.exception.ErrorCode;
import com.example.hospitalClinical.encounter.dto.ClinicalVitalAssessResponse;
import com.example.hospitalClinical.encounter.dto.ClinicalVitalAssessSaveRequest;
import com.example.hospitalClinical.encounter.entity.ClinicalVitalAssess;
import com.example.hospitalClinical.encounter.entity.Visit;
import com.example.hospitalClinical.encounter.exception.VisitNotFoundException;
import com.example.hospitalClinical.encounter.repository.ClinicalVitalAssessRepo;
import com.example.hospitalClinical.encounter.repository.VisitRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClinicalVitalAssessServiceImpl implements ClinicalVitalAssessService {

    private final VisitRepo visitRepo;
    private final ClinicalVitalAssessRepo clinicalVitalAssessRepo;

    @Override
    public Optional<ClinicalVitalAssessResponse> getByVisitId(Long visitId) {
        visitRepo.findById(visitId).orElseThrow(VisitNotFoundException::new);
        return clinicalVitalAssessRepo.findByVisitId(visitId).map(ClinicalVitalAssessResponse::from);
    }

    @Override
    @Transactional
    public ClinicalVitalAssessResponse upsert(Long visitId, ClinicalVitalAssessSaveRequest request) {
        Visit visit = visitRepo.findById(visitId).orElseThrow(VisitNotFoundException::new);
        if (request.getVisitId() != null && !request.getVisitId().equals(visitId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "visitId가 경로와 일치하지 않습니다.");
        }
        ClinicalVitalAssess entity =
                clinicalVitalAssessRepo
                        .findByVisitId(visitId)
                        .orElseGet(() -> ClinicalVitalAssess.createNew(visitId, visit.getReceptionId()));
        entity.applySave(request);
        ClinicalVitalAssess saved = clinicalVitalAssessRepo.save(entity);
        return ClinicalVitalAssessResponse.from(saved);
    }
}
