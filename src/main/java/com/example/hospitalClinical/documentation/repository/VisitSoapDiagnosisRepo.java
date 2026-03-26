package com.example.hospitalClinical.documentation.repository;

import com.example.hospitalClinical.documentation.entity.VisitSoapDiagnosis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VisitSoapDiagnosisRepo extends JpaRepository<VisitSoapDiagnosis, Long> {

    List<VisitSoapDiagnosis> findByVisitIdOrderBySortOrderAscDiagnosisIdAsc(Long visitId);

    Optional<VisitSoapDiagnosis> findByDiagnosisIdAndVisitId(Long diagnosisId, Long visitId);

    void deleteByVisitId(Long visitId);
}
