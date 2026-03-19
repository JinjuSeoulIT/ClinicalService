package com.example.hospitalClinical.encounter.service;

import com.example.hospitalClinical.encounter.dto.VisitCreateRequest;
import com.example.hospitalClinical.encounter.entity.Visit;
import com.example.hospitalClinical.encounter.entity.VisitQueue;
import com.example.hospitalClinical.encounter.entity.VisitStatusHistory;

import java.util.List;

public interface EncounterService {

    Visit createVisit(VisitCreateRequest request);
    Visit getVisit(Long visitId);
    Visit updateVisitStatus(Long visitId, String visitStatus);
    Visit endVisit(Long visitId);
    List<Visit> listByPatientId(Long patientId);
    List<Visit> listByReceptionId(Long receptionId);
    List<Visit> listByStatus(String visitStatus);
    List<Visit> listAll();

    VisitStatusHistory createStatusHistory(Long visitId, String status);
    VisitStatusHistory getStatusHistory(Long historyId);
    List<VisitStatusHistory> listStatusHistoryByVisitId(Long visitId);

    VisitQueue createQueue(Long visitId, Integer queueOrder, Long roomId);
    VisitQueue getQueue(Long queueId);
    List<VisitQueue> listQueueByVisitId(Long visitId);
    List<VisitQueue> listAllQueue();
}
