package com.example.hospitalClinical.documentation.repository;

import com.example.hospitalClinical.documentation.entity.NoteAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteAttachmentRepo extends JpaRepository<NoteAttachment, Long> {

    List<NoteAttachment> findByNoteIdOrderByCreatedAtDesc(Long noteId);
}
