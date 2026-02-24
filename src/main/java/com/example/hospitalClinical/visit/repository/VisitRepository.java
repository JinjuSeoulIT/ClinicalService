package com.example.hospitalClinical.visit.repository;

import com.example.hospitalClinical.visit.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VisitRepository extends JpaRepository<Visit, Long> {
    List<Visit> findByPatientIdOrderByVisitAtDesc(Long patientId);
}