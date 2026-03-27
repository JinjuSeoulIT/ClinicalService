package com.example.hospitalClinical.encounter.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "CLINICAL_VISIT_STATUS_HISTORY")
public class VisitStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "visit_status_history_seq_gen")
    @SequenceGenerator(name = "visit_status_history_seq_gen", sequenceName = "CL_VISIT_STATUS_HIST_SEQ", allocationSize = 1)
    @Column(name = "HISTORY_ID", nullable = false)
    private Long historyId;

    @Column(name = "VISIT_ID", nullable = false)
    private Long visitId;

    @Column(name = "STATUS", length = 20)
    private String status;

    @Column(name = "CHANGED_AT")
    private LocalDateTime changedAt;

    protected VisitStatusHistory() {}

    public static VisitStatusHistory create(Long visitId, String status) {
        VisitStatusHistory h = new VisitStatusHistory();
        h.visitId = visitId;
        h.status = status != null ? status : "WAITING";
        return h;
    }

    @PrePersist
    void prePersist() {
        if (changedAt == null) changedAt = LocalDateTime.now();
    }

    public Long getHistoryId() { return historyId; }
    public Long getVisitId() { return visitId; }
    public String getStatus() { return status; }
    public LocalDateTime getChangedAt() { return changedAt; }
}
