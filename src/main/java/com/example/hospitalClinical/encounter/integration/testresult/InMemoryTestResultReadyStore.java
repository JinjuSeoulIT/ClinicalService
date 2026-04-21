package com.example.hospitalClinical.encounter.integration.testresult;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryTestResultReadyStore implements TestResultReadyStore {

    private final Map<Long, TestResultReadyEntry> byVisitId = new ConcurrentHashMap<>();
    private final Map<Long, TestResultReadyEntry> byPatientId = new ConcurrentHashMap<>();

    @Override
    public void put(TestResultReadyEntry entry) {
        if (entry == null) {
            return;
        }
        if (entry.getVisitId() != null) {
            byVisitId.put(entry.getVisitId(), entry);
        }
        if (entry.getPatientId() != null) {
            byPatientId.put(entry.getPatientId(), entry);
        }
    }

    @Override
    public Optional<TestResultReadyEntry> peek(Long visitId, Long visitPatientId) {
        if (visitPatientId == null) {
            return Optional.empty();
        }
        TestResultReadyEntry byVisit = visitId != null ? byVisitId.get(visitId) : null;
        if (byVisit != null && patientMatches(byVisit, visitPatientId)) {
            return Optional.of(byVisit);
        }
        TestResultReadyEntry byPatient = byPatientId.get(visitPatientId);
        if (byPatient == null) {
            return Optional.empty();
        }
        if (byPatient.getVisitId() != null && visitId != null && !byPatient.getVisitId().equals(visitId)) {
            return Optional.empty();
        }
        return Optional.of(byPatient);
    }

    @Override
    public void clear(Long visitId, Long patientId) {
        if (visitId != null) {
            byVisitId.remove(visitId);
        }
        if (patientId != null) {
            byPatientId.remove(patientId);
        }
    }

    private static boolean patientMatches(TestResultReadyEntry entry, Long visitPatientId) {
        if (entry.getPatientId() == null) {
            return true;
        }
        return entry.getPatientId().equals(visitPatientId);
    }
}
