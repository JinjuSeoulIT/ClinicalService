package com.example.hospitalClinical.documentation.dto;

import com.example.hospitalClinical.documentation.entity.SoapDx;

public record SoapDxResponse(
        Long diagnosisId,
        Long clinicalId,
        String dxCode,
        String dxName,
        String dxSource,
        String mainYn,
        Integer sortOrder
) {
    public static SoapDxResponse from(SoapDx d) {
        return new SoapDxResponse(
                d.getDiagnosisId(),
                d.getVisitId(),
                d.getDxCode(),
                d.getDxName(),
                d.getDxSource() != null ? d.getDxSource().name() : null,
                d.getMainYn(),
                d.getSortOrder()
        );
    }
}
