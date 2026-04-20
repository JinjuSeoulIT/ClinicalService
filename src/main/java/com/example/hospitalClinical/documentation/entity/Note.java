package com.example.hospitalClinical.documentation.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "NOTE")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "clinical_note_seq_gen")
    @SequenceGenerator(name = "clinical_note_seq_gen", sequenceName = "CL_NOTE_SEQ", allocationSize = 1)
    @Column(name = "NOTE_ID", nullable = false)
    private Long noteId;

    @Column(name = "VISIT_ID", nullable = false)
    private Long visitId;

    @Column(name = "CHIEF_COMPLAINT", length = 2000)
    private String chiefComplaint;

    @Column(name = "PRESENT_ILLNESS", length = 4000)
    private String presentIllness;

    @Column(name = "MEMO", length = 2000)
    private String memo;

    @Column(name = "STATUS", length = 20)
    private String status;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    protected Note() {}

    public static Note create(Long visitId) {
        Note n = new Note();
        n.visitId = visitId;
        n.status = "DRAFT";
        return n;
    }

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void setChiefComplaint(String chiefComplaint) { this.chiefComplaint = chiefComplaint; }
    public void setPresentIllness(String presentIllness) { this.presentIllness = presentIllness; }
    public void setMemo(String memo) { this.memo = memo; }
    public void setStatus(String status) { this.status = status; }

    public Long getNoteId() { return noteId; }
    public Long getVisitId() { return visitId; }
    public String getChiefComplaint() { return chiefComplaint; }
    public String getPresentIllness() { return presentIllness; }
    public String getMemo() { return memo; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
