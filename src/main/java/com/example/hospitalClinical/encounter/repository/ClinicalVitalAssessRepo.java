package com.example.hospitalClinical.encounter.repository;

import com.example.hospitalClinical.encounter.entity.ClinicalVitalAssess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClinicalVitalAssessRepo extends JpaRepository<ClinicalVitalAssess, Long> {

    Optional<ClinicalVitalAssess> findByVisitId(Long visitId);
}
