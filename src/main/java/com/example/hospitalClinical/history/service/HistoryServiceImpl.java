package com.example.hospitalClinical.history.service;

import com.example.hospitalClinical.encounter.entity.Visit;
import com.example.hospitalClinical.encounter.service.EncounterService;
import com.example.hospitalClinical.history.dto.HistoryUpdateRequest;
import com.example.hospitalClinical.history.entity.History;
import com.example.hospitalClinical.history.repository.HistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {

    private final HistoryRepository historyRepository;
    private final EncounterService encounterService;

    @Override
    @Transactional(readOnly = true)
    public List<History> listByPatientId(Long patientId) {
        return historyRepository.findByPatientIdOrderByIdAsc(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<History> listByVisitId(Long visitId) {
        Visit visit = encounterService.getVisit(visitId);
        return listByPatientId(visit.getPatientId());
    }

    @Override
    @Transactional
    public History saveByVisitId(Long visitId, HistoryUpdateRequest request) {
        Visit visit = encounterService.getVisit(visitId);
        History entity = History.builder()
                .patientId(visit.getPatientId())
                .historyType(request.getHistoryType())
                .name(request.getName())
                .memo(request.getMemo())
                .build();
        return historyRepository.save(entity);
    }

    @Override
    @Transactional
    public History update(Long id, HistoryUpdateRequest request) {
        History entity = historyRepository.findById(id).orElseThrow();
        if (request.getHistoryType() != null) entity.setHistoryType(request.getHistoryType());
        if (request.getName() != null) entity.setName(request.getName());
        if (request.getMemo() != null) entity.setMemo(request.getMemo());
        return historyRepository.save(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        historyRepository.deleteById(id);
    }
}
