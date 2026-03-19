package com.example.hospitalClinical.encounter.repository;

import com.example.hospitalClinical.encounter.entity.VisitStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VisitStatusHistoryRepo extends JpaRepository<VisitStatusHistory, Long> {

    List<VisitStatusHistory> findByVisitIdOrderByChangedAtDesc(Long visitId);
}
