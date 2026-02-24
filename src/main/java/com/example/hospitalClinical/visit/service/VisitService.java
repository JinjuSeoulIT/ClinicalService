package com.example.hospitalClinical.visit.service;

import com.example.hospitalClinical.visit.dto.request.VisitCreateRequest;
import com.example.hospitalClinical.visit.entity.Visit;

import java.util.List;

public interface VisitService {
    Visit create(VisitCreateRequest req);
    Visit get(Long visitId);
    List<Visit> listByPatientId(Long patientId);
}
