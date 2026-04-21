package com.example.hospitalClinical.encounter.integration.testresult;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TestResultReadyEntry {
    Long visitId;
    Long patientId;
    String resultId;
    String resultType;
    Long orderItemId;
    long receivedAtEpochMilli;
}
