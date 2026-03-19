package com.example.hospitalClinical.documentation.dto;

import com.example.hospitalClinical.documentation.entity.NoteAttachment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteAttachmentResponse {
    private Long attachmentId;
    private Long noteId;
    private String fileName;
    private String filePath;
    private String fileType;
    private LocalDateTime createdAt;

    public static NoteAttachmentResponse from(NoteAttachment a) {
        return new NoteAttachmentResponse(
                a.getAttachmentId(), a.getNoteId(), a.getFileName(), a.getFilePath(),
                a.getFileType(), a.getCreatedAt()
        );
    }
}
