package com.example.hospitalClinical.visit.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "VISIT", schema = "HOSPITAL")
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "visit_seq_gen")
    @SequenceGenerator(
            name = "visit_seq_gen",
            sequenceName = "VISIT_SEQ",
            allocationSize = 1
    )
    @Column(name = "VISIT_ID")
    private Long visitId;

    @Column(name = "PATIENT_ID", nullable = false)
    private Long patientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "VISIT_TYPE", nullable = false, length = 30)
    private VisitType visitType;

    @Enumerated(EnumType.STRING)
    @Column(name = "VISIT_STATUS", nullable = false, length = 30)
    private VisitStatus visitStatus;

    @Column(name = "VISIT_AT", nullable = false)
    private LocalDateTime visitAt;

    @Column(name = "DEPT_ID")
    private Long deptId;

    @Column(name = "DOCTOR_ID")
    private Long doctorId;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    protected Visit() {
    }

    public static Visit create(Long patientId, VisitType visitType, LocalDateTime visitAt, Long deptId, Long doctorId) {
        Visit v = new Visit();
        v.patientId = patientId;
        v.visitType = (visitType == null) ? VisitType.OUT : visitType;
        v.visitStatus = VisitStatus.READY;
        v.visitAt = visitAt; // null이면 @PrePersist에서 now로 채움
        v.deptId = deptId;
        v.doctorId = doctorId;
        return v;
    }

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (visitStatus == null) visitStatus = VisitStatus.READY;
        if (visitAt == null) visitAt = now;
        if (visitType == null) visitType = VisitType.OUT;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getVisitId() { return visitId; }
    public Long getPatientId() { return patientId; }
    public VisitType getVisitType() { return visitType; }
    public VisitStatus getVisitStatus() { return visitStatus; }
    public LocalDateTime getVisitAt() { return visitAt; }
    public Long getDeptId() { return deptId; }
    public Long getDoctorId() { return doctorId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}