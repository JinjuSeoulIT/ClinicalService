package com.example.hospitalClinical.documentation.service;

import com.example.hospitalClinical.documentation.dto.DrugSearchResult;
import com.example.hospitalClinical.documentation.dto.StandardDiagnosisItemDto;
import com.example.hospitalClinical.documentation.dto.SoapDxRequest;
import com.example.hospitalClinical.documentation.dto.SoapDxResponse;
import com.example.hospitalClinical.documentation.dto.SoapRxRequest;
import com.example.hospitalClinical.documentation.dto.SoapRxResponse;
import com.example.hospitalClinical.documentation.entity.Diagnosis;
import com.example.hospitalClinical.documentation.entity.Note;
import com.example.hospitalClinical.documentation.entity.NoteAttachment;
import com.example.hospitalClinical.documentation.entity.NoteHistory;

import java.util.List;
import java.util.Optional;

public interface ChartService {

    Note createNote(Long visitId);

    Note getNote(Long noteId);

    Note getNoteByVisitId(Long visitId);

    Optional<Note> findNoteByVisitId(Long visitId);

    List<Note> listNotesByVisitId(Long visitId);

    Note updateNote(Long noteId, String chiefComplaint, String presentIllness, String assessment, String plan,
                    String memo, String status);

    Diagnosis createDiagnosis(Long noteId, String patientCode, String diagnosisCode, String description);

    Diagnosis getDiagnosis(Long diagnosisId);

    List<Diagnosis> listDiagnosisByNoteId(Long noteId);

    NoteHistory createNoteHistory(Long noteId, String changeType, Long changedBy);

    NoteHistory getNoteHistory(Long historyId);

    List<NoteHistory> listNoteHistoryByNoteId(Long noteId);

    NoteAttachment createAttachment(Long noteId, String fileName, String filePath, String fileType);

    NoteAttachment getAttachment(Long attachmentId);

    List<NoteAttachment> listAttachmentByNoteId(Long noteId);

    DrugSearchResult searchDrugs(Integer pageNo, Integer numOfRows, String itemName);

    List<SoapDxResponse> listSoapDx(Long visitId);

    SoapDxResponse addSoapDx(Long visitId, SoapDxRequest request);

    void removeSoapDx(Long visitId, Long diagnosisId);

    SoapDxResponse setMainSoapDx(Long visitId, Long diagnosisId);

    void reorderSoapDx(Long visitId, List<Long> diagnosisIds);

    List<SoapRxResponse> listSoapRx(Long visitId);

    SoapRxResponse addSoapRx(Long visitId, SoapRxRequest request);

    void removeSoapRx(Long visitId, Long prescriptionId);

    void updateSoapRx(
            Long visitId,
            Long prescriptionId,
            String medicationName,
            String dosage,
            String days);

    List<StandardDiagnosisItemDto> searchStandardDiagnosisMasters(
            String query, Integer pageNo, Integer numOfRows, String diseaseType);
}
