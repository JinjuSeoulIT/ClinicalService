package com.example.hospitalClinical.encounter.dto;

import com.example.hospitalClinical.encounter.entity.VisitStatusHistory;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VisitStatusHistoryResponse {
    private Long historyId;
    private Long visitId;
    private String status;
    private LocalDateTime changedAt;

    public static VisitStatusHistoryResponse from(VisitStatusHistory h) {
        return new VisitStatusHistoryResponse(
                h.getHistoryId(),
                h.getVisitId(),
                h.getStatus(),
                h.getChangedAt()
        );
    }
}
