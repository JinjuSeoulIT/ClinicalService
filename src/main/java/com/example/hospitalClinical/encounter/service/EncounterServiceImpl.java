package com.example.hospitalClinical.encounter.service;

import com.example.hospitalClinical.encounter.dto.VisitCreateRequest;
import com.example.hospitalClinical.encounter.entity.Visit;
import com.example.hospitalClinical.encounter.entity.VisitQueue;
import com.example.hospitalClinical.encounter.entity.VisitStatusHistory;
import com.example.hospitalClinical.encounter.exception.VisitNotFoundException;
import com.example.hospitalClinical.encounter.repository.VisitQueueRepo;
import com.example.hospitalClinical.encounter.repository.VisitRepo;
import com.example.hospitalClinical.encounter.repository.VisitStatusHistoryRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EncounterServiceImpl implements EncounterService {

    private final VisitRepo visitRepo;
    private final VisitStatusHistoryRepo visitStatusHistoryRepo;
    private final VisitQueueRepo visitQueueRepo;

    @Override
    @Transactional
    public Visit createVisit(VisitCreateRequest request) {
        Visit v = Visit.create(
                request.getPatientId(),
                request.getDoctorId(),
                request.getReceptionId(),
                request.getVisitStatus()
        );
        if (request.getStartTime() != null) v.setStartTime(request.getStartTime());
        return visitRepo.save(v);
    }

    @Override
    public Visit getVisit(Long visitId) {
        return visitRepo.findById(visitId).orElseThrow(VisitNotFoundException::new);
    }

    @Override
    @Transactional
    public Visit updateVisitStatus(Long visitId, String visitStatus) {
        Visit v = visitRepo.findById(visitId).orElseThrow(VisitNotFoundException::new);
        v.setVisitStatus(visitStatus);
        return visitRepo.save(v);
    }

    @Override
    @Transactional
    public Visit endVisit(Long visitId) {
        Visit v = visitRepo.findById(visitId).orElseThrow(VisitNotFoundException::new);
        v.setEndTime(LocalDateTime.now());
        v.setVisitStatus("COMPLETED");
        return visitRepo.save(v);
    }

    @Override
    public List<Visit> listByPatientId(Long patientId) {
        return visitRepo.findByPatientIdOrderByStartTimeDesc(patientId);
    }

    @Override
    public List<Visit> listByReceptionId(Long receptionId) {
        return visitRepo.findByReceptionIdOrderByStartTimeDesc(receptionId);
    }

    @Override
    public List<Visit> listByStatus(String visitStatus) {
        return visitRepo.findByVisitStatusOrderByStartTimeAsc(visitStatus);
    }

    @Override
    public List<Visit> listAll() {
        return visitRepo.findAllByOrderByStartTimeDesc();
    }

    @Override
    @Transactional
    public VisitStatusHistory createStatusHistory(Long visitId, String status) {
        if (!visitRepo.existsById(visitId)) throw new VisitNotFoundException();
        return visitStatusHistoryRepo.save(VisitStatusHistory.create(visitId, status));
    }

    @Override
    public VisitStatusHistory getStatusHistory(Long historyId) {
        return visitStatusHistoryRepo.findById(historyId)
                .orElseThrow(() -> new IllegalArgumentException("VisitStatusHistory not found: " + historyId));
    }

    @Override
    public List<VisitStatusHistory> listStatusHistoryByVisitId(Long visitId) {
        return visitStatusHistoryRepo.findByVisitIdOrderByChangedAtDesc(visitId);
    }

    @Override
    @Transactional
    public VisitQueue createQueue(Long visitId, Integer queueOrder, Long roomId) {
        if (!visitRepo.existsById(visitId)) throw new VisitNotFoundException();
        return visitQueueRepo.save(VisitQueue.create(visitId, queueOrder, roomId));
    }

    @Override
    public VisitQueue getQueue(Long queueId) {
        return visitQueueRepo.findById(queueId)
                .orElseThrow(() -> new IllegalArgumentException("VisitQueue not found: " + queueId));
    }

    @Override
    public List<VisitQueue> listQueueByVisitId(Long visitId) {
        return visitQueueRepo.findByVisitIdOrderByQueueOrderAsc(visitId);
    }

    @Override
    public List<VisitQueue> listAllQueue() {
        return visitQueueRepo.findAllByOrderByQueueOrderAsc();
    }
}
