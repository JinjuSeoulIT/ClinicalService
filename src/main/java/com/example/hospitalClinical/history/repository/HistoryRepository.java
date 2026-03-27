package com.example.hospitalClinical.history.repository;

import com.example.hospitalClinical.history.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoryRepository extends JpaRepository<History, Long> {

    List<History> findByPatientIdOrderByIdAsc(Long patientId);
}
