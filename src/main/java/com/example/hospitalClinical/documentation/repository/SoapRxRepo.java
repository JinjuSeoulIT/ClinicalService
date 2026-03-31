package com.example.hospitalClinical.documentation.repository;

import com.example.hospitalClinical.documentation.entity.SoapRx;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SoapRxRepo extends JpaRepository<SoapRx, Long> {

    List<SoapRx> findByVisitIdOrderByPrescriptionIdAsc(Long visitId);

    Optional<SoapRx> findByPrescriptionIdAndVisitId(Long prescriptionId, Long visitId);

    void deleteByVisitId(Long visitId);
}
