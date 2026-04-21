package com.example.hospitalClinical.documentation.dto;

import com.example.hospitalClinical.documentation.entity.SoapRx;

public record SoapRxResponse(
        Long prescriptionId,
        Long clinicalId,
        String medicationName,
        String dosage,
        String frequency,
        String days
) {
    public static SoapRxResponse from(SoapRx p) {
        return new SoapRxResponse(
                p.getPrescriptionId(),
                p.getVisitId(),
                p.getMedicationName(),
                p.getDosage(),
                p.getFrequency(),
                p.getDays()
        );
    }
}
