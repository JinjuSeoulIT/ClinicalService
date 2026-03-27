package com.example.hospitalClinical.history.dto;

import com.example.hospitalClinical.history.entity.History;
import com.example.hospitalClinical.history.entity.HistoryType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class HistoryResponse {

    private Long id;
    private Long patientId;
    private HistoryType historyType;
    private String name;
    private String memo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static HistoryResponse from(History entity) {
        if (entity == null) return null;
        return HistoryResponse.builder()
                .id(entity.getId())
                .patientId(entity.getPatientId())
                .historyType(entity.getHistoryType())
                .name(entity.getName())
                .memo(entity.getMemo())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
