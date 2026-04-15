package com.example.hospitalClinical.common.client.external.clinicalsupport;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MedicationRecordOutboundRequest {

    private String medicationId;
    private Long patientId;
    private String patientName;
    private String departmentName;
    private Double doseNumber;
    private String doseUnit;
    private String doseKind;
    private String status;
    private String progressStatus;
}
