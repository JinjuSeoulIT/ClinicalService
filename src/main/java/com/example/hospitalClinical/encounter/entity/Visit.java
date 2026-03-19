package com.example.hospitalClinical.encounter.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "CLINICAL_VISIT")
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "visit_seq_gen")
    @SequenceGenerator(name = "visit_seq_gen", sequenceName = "CL_VISIT_SEQ", allocationSize = 1)
    @Column(name = "VISIT_ID")
    private Long visitId;

    @Column(name = "PATIENT_ID")
    private Long patientId;

    @Column(name = "DOCTOR_ID")
    private Long doctorId;

    @Column(name = "RECEPTION_ID")
    private Long receptionId;

    @Column(name = "VISIT_STATUS", length = 20)
    private String visitStatus;

    @Column(name = "START_TIME")
    private LocalDateTime startTime;

    @Column(name = "END_TIME")
    private LocalDateTime endTime;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    protected Visit() {}

    public static Visit create(Long patientId, Long doctorId, Long receptionId, String visitStatus) {
        Visit v = new Visit();
        v.patientId = patientId;
        v.doctorId = doctorId;
        v.receptionId = receptionId;
        v.visitStatus = visitStatus != null ? visitStatus : "WAITING";
        v.startTime = LocalDateTime.now();
        return v;
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

    public Long getVisitId() { return visitId; }
    public Long getPatientId() { return patientId; }
    public Long getDoctorId() { return doctorId; }
    public Long getReceptionId() { return receptionId; }
    public String getVisitStatus() { return visitStatus; }
    public void setVisitStatus(String visitStatus) { this.visitStatus = visitStatus; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
