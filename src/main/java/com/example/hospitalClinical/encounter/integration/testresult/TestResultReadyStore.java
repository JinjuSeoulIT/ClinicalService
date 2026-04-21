package com.example.hospitalClinical.encounter.integration.testresult;

import java.util.Optional;

public interface TestResultReadyStore {
    void put(TestResultReadyEntry entry);

    Optional<TestResultReadyEntry> peek(Long visitId, Long visitPatientId);

    void clear(Long visitId, Long patientId);
}
