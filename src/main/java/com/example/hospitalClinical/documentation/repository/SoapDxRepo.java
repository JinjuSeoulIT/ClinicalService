package com.example.hospitalClinical.documentation.repository;

import com.example.hospitalClinical.documentation.entity.SoapDx;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SoapDxRepo extends JpaRepository<SoapDx, Long> {

    List<SoapDx> findByVisitIdOrderBySortOrderAscDiagnosisIdAsc(Long visitId);

    Optional<SoapDx> findByDiagnosisIdAndVisitId(Long diagnosisId, Long visitId);

    void deleteByVisitId(Long visitId);
}
