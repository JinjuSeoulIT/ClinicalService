package com.example.hospitalClinical.documentation.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "NOTE_HISTORY")
public class NoteHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "clinical_note_history_seq_gen")
    @SequenceGenerator(name = "clinical_note_history_seq_gen", sequenceName = "CL_NOTE_HIST_SEQ", allocationSize = 1)
    @Column(name = "HISTORY_ID", nullable = false)
    private Long historyId;

    @Column(name = "NOTE_ID", nullable = false)
    private Long noteId;

    @Column(name = "CHANGE_TYPE", length = 20)
    private String changeType;

    @Column(name = "CHANGED_BY")
    private Long changedBy;

    @Column(name = "CHANGED_AT")
    private LocalDateTime changedAt;

    protected NoteHistory() {}

    public static NoteHistory create(Long noteId, String changeType, Long changedBy) {
        NoteHistory h = new NoteHistory();
        h.noteId = noteId;
        h.changeType = changeType;
        h.changedBy = changedBy;
        return h;
    }

    @PrePersist
    void prePersist() {
        if (changedAt == null) changedAt = LocalDateTime.now();
    }

    public Long getHistoryId() { return historyId; }
    public Long getNoteId() { return noteId; }
    public String getChangeType() { return changeType; }
    public Long getChangedBy() { return changedBy; }
    public LocalDateTime getChangedAt() { return changedAt; }
}
