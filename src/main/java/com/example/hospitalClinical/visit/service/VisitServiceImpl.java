package com.example.hospitalClinical.visit.service;

import com.example.hospitalClinical.visit.dto.request.VisitCreateRequest;
import com.example.hospitalClinical.visit.entity.Visit;
import com.example.hospitalClinical.visit.entity.VisitType;
import com.example.hospitalClinical.visit.exception.VisitNotFoundException;
import com.example.hospitalClinical.visit.repository.VisitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VisitServiceImpl implements VisitService {

    private final VisitRepository visitRepository;

    public VisitServiceImpl(VisitRepository visitRepository) {
        this.visitRepository = visitRepository;
    }

    @Override
    @Transactional
    public Visit create(VisitCreateRequest req) {
        if (req.getPatientId() == null) {
            throw new IllegalArgumentException("patientId는 필수입니다.");
        }

        VisitType type = (req.getVisitType() == null) ? VisitType.OUT : req.getVisitType();
        LocalDateTime visitAt = req.getVisitAt(); // null이면 엔티티에서 now 처리

        Visit visit = Visit.create(
                req.getPatientId(),
                type,
                visitAt,
                req.getDeptId(),
                req.getDoctorId()
        );

        return visitRepository.save(visit);
    }

    @Override
    @Transactional(readOnly = true)
    public Visit get(Long visitId) {
        return visitRepository.findById(visitId)
                .orElseThrow(VisitNotFoundException::new);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Visit> listByPatientId(Long patientId) {
        return visitRepository.findByPatientIdOrderByVisitAtDesc(patientId);
    }
}