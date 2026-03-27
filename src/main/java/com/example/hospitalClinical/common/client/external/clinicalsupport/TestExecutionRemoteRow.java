package com.example.hospitalClinical.common.client.external.clinicalsupport;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestExecutionRemoteRow {

    private String testExecutionId;

    private Long orderItemId;

    private String executionType;

    private String progressStatus;

    private Integer retryNo;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private Long performerId;

    private LocalDateTime updatedAt;
}
