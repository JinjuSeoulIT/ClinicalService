package com.example.hospitalClinical.encounter.dto;

import com.example.hospitalClinical.encounter.entity.VisitQueue;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VisitQueueResponse {
    private Long queueId;
    private Long visitId;
    private Integer queueOrder;
    private Long roomId;
    private LocalDateTime createdAt;

    public static VisitQueueResponse from(VisitQueue q) {
        return new VisitQueueResponse(
                q.getQueueId(),
                q.getVisitId(),
                q.getQueueOrder(),
                q.getRoomId(),
                q.getCreatedAt()
        );
    }
}
