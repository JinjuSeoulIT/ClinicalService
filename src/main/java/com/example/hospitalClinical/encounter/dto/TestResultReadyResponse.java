package com.example.hospitalClinical.encounter.dto;

import com.example.hospitalClinical.encounter.integration.testresult.TestResultReadyEntry;
import lombok.Value;

@Value
public class TestResultReadyResponse {
    Long visitId;
    Long patientId;
    String resultId;
    String resultType;
    Long orderItemId;
    long receivedAtEpochMilli;

    public static TestResultReadyResponse from(TestResultReadyEntry e) {
        return new TestResultReadyResponse(
                e.getVisitId(),
                e.getPatientId(),
                e.getResultId(),
                e.getResultType(),
                e.getOrderItemId(),
                e.getReceivedAtEpochMilli());
    }
}
