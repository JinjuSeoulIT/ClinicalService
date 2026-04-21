package com.example.hospitalClinical.encounter.repository;

import com.example.hospitalClinical.encounter.entity.VitalSaveAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VitalSaveAuditRepo extends JpaRepository<VitalSaveAudit, Long> {

    List<VitalSaveAudit> findByVisitIdOrderBySaveAuditIdAsc(Long visitId);
}
