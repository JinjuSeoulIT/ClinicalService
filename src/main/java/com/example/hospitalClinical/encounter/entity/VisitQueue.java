package com.example.hospitalClinical.encounter.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "CLINICAL_VISIT_QUEUE")
public class VisitQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "visit_queue_seq_gen")
    @SequenceGenerator(name = "visit_queue_seq_gen", sequenceName = "CL_VISIT_QUEUE_SEQ", allocationSize = 1)
    @Column(name = "QUEUE_ID", nullable = false)
    private Long queueId;

    @Column(name = "VISIT_ID", nullable = false)
    private Long visitId;

    @Column(name = "QUEUE_ORDER")
    private Integer queueOrder;

    @Column(name = "ROOM_ID")
    private Long roomId;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    protected VisitQueue() {}

    public static VisitQueue create(Long visitId, Integer queueOrder, Long roomId) {
        VisitQueue q = new VisitQueue();
        q.visitId = visitId;
        q.queueOrder = queueOrder;
        q.roomId = roomId;
        return q;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public Long getQueueId() { return queueId; }
    public Long getVisitId() { return visitId; }
    public Integer getQueueOrder() { return queueOrder; }
    public Long getRoomId() { return roomId; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void relocate(Integer queueOrder, Long roomId) {
        this.queueOrder = queueOrder;
        this.roomId = roomId;
    }
}
