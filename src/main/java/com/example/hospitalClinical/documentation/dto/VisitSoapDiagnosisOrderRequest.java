package com.example.hospitalClinical.documentation.dto;

import java.util.List;

public class VisitSoapDiagnosisOrderRequest {

    private List<Long> diagnosisIds;

    public List<Long> getDiagnosisIds() { return diagnosisIds; }
    public void setDiagnosisIds(List<Long> diagnosisIds) { this.diagnosisIds = diagnosisIds; }
}
