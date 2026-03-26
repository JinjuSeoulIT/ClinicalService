package com.example.hospitalClinical.documentation.dto;

import com.example.hospitalClinical.documentation.entity.VisitSoapDiagnosis;

public record VisitSoapDiagnosisResponse(
        Long diagnosisId,
        Long clinicalId,
        String dxCode,
        String dxName,
        String mainYn,
        Integer sortOrder
) {
    public static VisitSoapDiagnosisResponse from(VisitSoapDiagnosis d) {
        return new VisitSoapDiagnosisResponse(
                d.getDiagnosisId(),
                d.getVisitId(),
                d.getDxCode(),
                d.getDxName(),
                d.getMainYn(),
                d.getSortOrder()
        );
    }
}
