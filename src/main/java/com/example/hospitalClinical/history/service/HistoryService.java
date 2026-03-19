package com.example.hospitalClinical.history.service;

import com.example.hospitalClinical.history.dto.HistoryUpdateRequest;
import com.example.hospitalClinical.history.entity.History;

import java.util.List;

public interface HistoryService {

    List<History> listByPatientId(Long patientId);
    List<History> listByVisitId(Long visitId);
    History saveByVisitId(Long visitId, HistoryUpdateRequest request);
    History update(Long id, HistoryUpdateRequest request);
    void delete(Long id);
}
