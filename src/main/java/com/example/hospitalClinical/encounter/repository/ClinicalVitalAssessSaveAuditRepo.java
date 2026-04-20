package com.example.hospitalClinical.encounter.repository;

import com.example.hospitalClinical.encounter.entity.ClinicalVitalAssessSaveAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClinicalVitalAssessSaveAuditRepo extends JpaRepository<ClinicalVitalAssessSaveAudit, Long> {

    List<ClinicalVitalAssessSaveAudit> findByVisitIdOrderBySaveAuditIdAsc(Long visitId);
}
