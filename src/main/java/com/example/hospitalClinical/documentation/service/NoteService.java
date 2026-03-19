package com.example.hospitalClinical.documentation.service;

import com.example.hospitalClinical.documentation.entity.Note;
import com.example.hospitalClinical.documentation.entity.NoteAttachment;
import com.example.hospitalClinical.documentation.entity.NoteHistory;
import com.example.hospitalClinical.documentation.entity.Diagnosis;

import java.util.List;
import java.util.Optional;

public interface NoteService {

    Note createNote(Long visitId);
    Note getNote(Long noteId);
    Note getNoteByVisitId(Long visitId);
    Optional<Note> findNoteByVisitId(Long visitId);
    List<Note> listNotesByVisitId(Long visitId);
    Note updateNote(Long noteId, String chiefComplaint, String presentIllness, String assessment, String plan, String memo, String status);

    Diagnosis createDiagnosis(Long noteId, String patientCode, String diagnosisCode, String description);
    Diagnosis getDiagnosis(Long diagnosisId);
    List<Diagnosis> listDiagnosisByNoteId(Long noteId);

    NoteHistory createNoteHistory(Long noteId, String changeType, Long changedBy);
    NoteHistory getNoteHistory(Long historyId);
    List<NoteHistory> listNoteHistoryByNoteId(Long noteId);

    NoteAttachment createAttachment(Long noteId, String fileName, String filePath, String fileType);
    NoteAttachment getAttachment(Long attachmentId);
    List<NoteAttachment> listAttachmentByNoteId(Long noteId);
}
