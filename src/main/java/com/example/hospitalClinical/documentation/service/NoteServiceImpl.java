package com.example.hospitalClinical.documentation.service;

import com.example.hospitalClinical.documentation.entity.Diagnosis;
import com.example.hospitalClinical.documentation.entity.Note;
import com.example.hospitalClinical.documentation.entity.NoteAttachment;
import com.example.hospitalClinical.documentation.entity.NoteHistory;
import com.example.hospitalClinical.documentation.exception.NoteNotFoundException;
import com.example.hospitalClinical.documentation.repository.DiagnosisRepo;
import com.example.hospitalClinical.documentation.repository.NoteAttachmentRepo;
import com.example.hospitalClinical.documentation.repository.NoteHistoryRepo;
import com.example.hospitalClinical.documentation.repository.NoteRepo;
import com.example.hospitalClinical.encounter.repository.VisitRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class NoteServiceImpl implements NoteService {

    private final NoteRepo noteRepo;
    private final DiagnosisRepo diagnosisRepo;
    private final NoteHistoryRepo noteHistoryRepo;
    private final NoteAttachmentRepo noteAttachmentRepo;
    private final VisitRepo visitRepo;

    @Override
    @Transactional
    public Note createNote(Long visitId) {
        if (!visitRepo.existsById(visitId)) throw new IllegalArgumentException("Visit not found: " + visitId);
        return noteRepo.save(Note.create(visitId));
    }

    @Override
    public Note getNote(Long noteId) {
        return noteRepo.findById(noteId).orElseThrow(NoteNotFoundException::new);
    }

    @Override
    public Note getNoteByVisitId(Long visitId) {
        return noteRepo.findByVisitId(visitId).orElseThrow(NoteNotFoundException::new);
    }

    @Override
    public Optional<Note> findNoteByVisitId(Long visitId) {
        return noteRepo.findByVisitId(visitId);
    }

    @Override
    public List<Note> listNotesByVisitId(Long visitId) {
        return noteRepo.findByVisitIdOrderByCreatedAtDesc(visitId);
    }

    @Override
    @Transactional
    public Note updateNote(Long noteId, String chiefComplaint, String presentIllness, String assessment, String plan, String memo, String status) {
        Note n = getNote(noteId);
        if (chiefComplaint != null) n.setChiefComplaint(chiefComplaint);
        if (presentIllness != null) n.setPresentIllness(presentIllness);
        if (assessment != null) n.setAssessment(assessment);
        if (plan != null) n.setPlan(plan);
        if (memo != null) n.setMemo(memo);
        if (status != null) n.setStatus(status);
        return noteRepo.save(n);
    }

    @Override
    @Transactional
    public Diagnosis createDiagnosis(Long noteId, String patientCode, String diagnosisCode, String description) {
        if (!noteRepo.existsById(noteId)) throw new NoteNotFoundException();
        return diagnosisRepo.save(Diagnosis.create(noteId, patientCode, diagnosisCode, description));
    }

    @Override
    public Diagnosis getDiagnosis(Long diagnosisId) {
        return diagnosisRepo.findById(diagnosisId)
                .orElseThrow(() -> new IllegalArgumentException("Diagnosis not found: " + diagnosisId));
    }

    @Override
    public List<Diagnosis> listDiagnosisByNoteId(Long noteId) {
        return diagnosisRepo.findByNoteIdOrderByCreatedAtDesc(noteId);
    }

    @Override
    @Transactional
    public NoteHistory createNoteHistory(Long noteId, String changeType, Long changedBy) {
        return noteHistoryRepo.save(NoteHistory.create(noteId, changeType, changedBy));
    }

    @Override
    public NoteHistory getNoteHistory(Long historyId) {
        return noteHistoryRepo.findById(historyId)
                .orElseThrow(() -> new IllegalArgumentException("NoteHistory not found: " + historyId));
    }

    @Override
    public List<NoteHistory> listNoteHistoryByNoteId(Long noteId) {
        return noteHistoryRepo.findByNoteIdOrderByChangedAtDesc(noteId);
    }

    @Override
    @Transactional
    public NoteAttachment createAttachment(Long noteId, String fileName, String filePath, String fileType) {
        if (!noteRepo.existsById(noteId)) throw new NoteNotFoundException();
        return noteAttachmentRepo.save(NoteAttachment.create(noteId, fileName, filePath, fileType));
    }

    @Override
    public NoteAttachment getAttachment(Long attachmentId) {
        return noteAttachmentRepo.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("NoteAttachment not found: " + attachmentId));
    }

    @Override
    public List<NoteAttachment> listAttachmentByNoteId(Long noteId) {
        return noteAttachmentRepo.findByNoteIdOrderByCreatedAtDesc(noteId);
    }
}
