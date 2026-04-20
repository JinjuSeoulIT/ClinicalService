package com.example.hospitalClinical.documentation.dto;

import com.example.hospitalClinical.documentation.entity.Note;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteResponse {
    private Long noteId;
    private Long visitId;
    private String chiefComplaint;
    private String presentIllness;
    private String memo;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NoteResponse from(Note n) {
        return new NoteResponse(
                n.getNoteId(), n.getVisitId(), n.getChiefComplaint(), n.getPresentIllness(),
                n.getMemo(), n.getStatus(),
                n.getCreatedAt(), n.getUpdatedAt()
        );
    }
}
