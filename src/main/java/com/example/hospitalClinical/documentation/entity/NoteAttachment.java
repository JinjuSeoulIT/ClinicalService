package com.example.hospitalClinical.documentation.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "NOTE_ATTACHMENT")
public class NoteAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "note_attachment_seq_gen")
    @SequenceGenerator(name = "note_attachment_seq_gen", sequenceName = "CL_NOTE_ATTACH_SEQ", allocationSize = 1)
    @Column(name = "ATTACHMENT_ID", nullable = false)
    private Long attachmentId;

    @Column(name = "NOTE_ID", nullable = false)
    private Long noteId;

    @Column(name = "FILE_NAME", length = 255)
    private String fileName;

    @Column(name = "FILE_PATH", length = 1000)
    private String filePath;

    @Column(name = "FILE_TYPE", length = 50)
    private String fileType;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    protected NoteAttachment() {}

    public static NoteAttachment create(Long noteId, String fileName, String filePath, String fileType) {
        NoteAttachment a = new NoteAttachment();
        a.noteId = noteId;
        a.fileName = fileName;
        a.filePath = filePath;
        a.fileType = fileType;
        return a;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public Long getAttachmentId() { return attachmentId; }
    public Long getNoteId() { return noteId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
