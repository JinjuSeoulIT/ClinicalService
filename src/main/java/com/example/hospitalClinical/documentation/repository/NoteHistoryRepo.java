package com.example.hospitalClinical.documentation.repository;

import com.example.hospitalClinical.documentation.entity.NoteHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteHistoryRepo extends JpaRepository<NoteHistory, Long> {

    List<NoteHistory> findByNoteIdOrderByChangedAtDesc(Long noteId);
}
