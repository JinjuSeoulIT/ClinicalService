package com.example.hospitalClinical.documentation.repository;

import com.example.hospitalClinical.documentation.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoteRepo extends JpaRepository<Note, Long> {

    Optional<Note> findByVisitId(Long visitId);
    List<Note> findByVisitIdOrderByCreatedAtDesc(Long visitId);
}
