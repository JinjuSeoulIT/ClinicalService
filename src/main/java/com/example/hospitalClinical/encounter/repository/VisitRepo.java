package com.example.hospitalClinical.encounter.repository;

import com.example.hospitalClinical.encounter.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VisitRepo extends JpaRepository<Visit, Long> {

    List<Visit> findByPatientIdOrderByStartTimeDesc(Long patientId);
    List<Visit> findByReceptionIdOrderByStartTimeDesc(Long receptionId);
    List<Visit> findByReceptionIdAndVisitStatus(Long receptionId, String visitStatus);
    List<Visit> findByVisitStatusOrderByStartTimeAsc(String visitStatus);
    List<Visit> findAllByOrderByStartTimeDesc();
}
