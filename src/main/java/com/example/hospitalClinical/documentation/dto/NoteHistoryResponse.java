package com.example.hospitalClinical.documentation.dto;

import com.example.hospitalClinical.documentation.entity.NoteHistory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteHistoryResponse {
    private Long historyId;
    private Long noteId;
    private String changeType;
    private Long changedBy;
    private LocalDateTime changedAt;

    public static NoteHistoryResponse from(NoteHistory h) {
        return new NoteHistoryResponse(
                h.getHistoryId(), h.getNoteId(), h.getChangeType(), h.getChangedBy(), h.getChangedAt()
        );
    }
}
