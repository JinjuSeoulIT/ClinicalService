package com.example.hospitalClinical.documentation.service;

import com.example.hospitalClinical.documentation.dto.DrugSearchResult;
import com.example.hospitalClinical.documentation.dto.HiraProcedureSearchResult;
import com.example.hospitalClinical.documentation.dto.StandardDiagnosisItemDto;
import com.example.hospitalClinical.documentation.dto.SoapDxRequest;
import com.example.hospitalClinical.documentation.dto.SoapDxResponse;
import com.example.hospitalClinical.documentation.dto.SoapRxRequest;
import com.example.hospitalClinical.documentation.dto.SoapRxResponse;
import com.example.hospitalClinical.documentation.entity.Diagnosis;
import com.example.hospitalClinical.documentation.entity.Note;

import java.util.List;
import java.util.Optional;

public interface DocumentationService {

    Note createNote(Long visitId);

    Note getNote(Long noteId);

    Note getNoteByVisitId(Long visitId);

    Optional<Note> findNoteByVisitId(Long visitId);

    List<Note> listNotesByVisitId(Long visitId);

    Note updateNote(Long noteId, String chiefComplaint, String presentIllness, String memo, String status);

    Diagnosis createDiagnosis(Long noteId, String patientCode, String diagnosisCode, String description);

    Diagnosis getDiagnosis(Long diagnosisId);

    List<Diagnosis> listDiagnosisByNoteId(Long noteId);

    DrugSearchResult searchDrugs(Integer pageNo, Integer numOfRows, String itemName, String itemSeq);

    HiraProcedureSearchResult searchProcedures(int pageNo, int numOfRows, String korNmQuery);

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
            String frequency,
            String days);

    Long saveSoapPrescriptionRow(
            Long visitId, String medicationName, String dosage, String frequency, String days);

    void replaceSoapPrescriptionFromOrder(
            Long visitId,
            Long prescriptionId,
            String medicationName,
            String dosage,
            String frequency,
            String days);

    List<StandardDiagnosisItemDto> searchStandardDiagnosisMasters(
            String query, Integer pageNo, Integer numOfRows, String diseaseType);
}
