package com.example.hospitalClinical.common.client.external.clinicalsupport;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestExecutionRegisterRequest {

    private String detailCode;

    private Long patientId;

    private String patientName;

    private  String departmentName;

    private Long orderItemId;

    private String executionType;

    private String progressStatus;

    private Integer retryNo;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private String performerId;
}
