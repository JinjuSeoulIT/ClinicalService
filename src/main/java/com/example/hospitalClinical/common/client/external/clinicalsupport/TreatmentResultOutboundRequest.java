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
public class TreatmentResultOutboundRequest {

    private String procedureResultId;
    private Long patientId;
    private String patientName;
    private String departmentName;
    private String status;
    private String progressStatus;
    private String detail;
}
