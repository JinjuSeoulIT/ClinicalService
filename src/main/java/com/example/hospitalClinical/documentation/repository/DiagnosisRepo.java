package com.example.hospitalClinical.documentation.repository;

import com.example.hospitalClinical.documentation.entity.Diagnosis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiagnosisRepo extends JpaRepository<Diagnosis, Long> {

    List<Diagnosis> findByNoteIdOrderByCreatedAtDesc(Long noteId);
}
