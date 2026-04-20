package com.example.hospitalClinical.encounter.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "CLINICAL_VISIT")
public class Visit {

    public static final String WAITING = "WAITING";
    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String COMPLETED = "COMPLETED";
    public static final String AUTO_CLOSED = "AUTO_CLOSED";

    private static final Set<String> ADMIN_STATUSES =
            Set.of(WAITING, IN_PROGRESS, COMPLETED, AUTO_CLOSED);

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "visit_seq_gen")
    @SequenceGenerator(
            name = "visit_seq_gen",
            sequenceName = "CL_VISIT_SEQ",
            allocationSize = 1
    )
    @Column(name = "VISIT_ID")
    private Long visitId;

    @Column(name = "PATIENT_ID", nullable = false)
    private Long patientId;

    @Column(name = "DOCTOR_ID", nullable = false, length = 30)
    private String doctorId;

    @Column(name = "RECEPTION_ID", nullable = false)
    private Long receptionId;

    @Column(name = "VISIT_STATUS", length = 20, nullable = false)
    private String visitStatus;

    @Column(name = "START_TIME")
    private LocalDateTime startTime;

    @Column(name = "END_TIME")
    private LocalDateTime endTime;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    protected Visit() {}

    public static boolean isTerminalStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return false;
        }
        String u = raw.trim().toUpperCase();
        return COMPLETED.equals(u) || AUTO_CLOSED.equals(u);
    }

    public boolean isTerminal() {
        return isTerminalStatus(this.visitStatus);
    }

    public static Visit create(Long patientId, String doctorId, Long receptionId) {
        if (patientId == null || doctorId == null || doctorId.isBlank() || receptionId == null) {
            throw new IllegalArgumentException("환자/의사/접수 정보는 필수입니다.");
        }
        Visit v = new Visit();
        v.patientId = patientId;
        v.doctorId = doctorId.trim();
        v.receptionId = receptionId;
        v.visitStatus = WAITING;
        v.startTime = null;
        v.endTime = null;
        return v;
    }

    public void start() {
        start(null);
    }

    public void start(LocalDateTime at) {
        if (!WAITING.equals(this.visitStatus)) {
            throw new IllegalStateException("대기 상태에서만 진료 시작 가능");
        }
        this.visitStatus = IN_PROGRESS;
        this.startTime = at != null ? at : LocalDateTime.now();
    }

    public void complete() {
        complete(null);
    }

    public void complete(LocalDateTime endAt) {
        if (!IN_PROGRESS.equals(this.visitStatus)) {
            throw new IllegalStateException("진료 중 상태에서만 종료 가능");
        }
        this.visitStatus = COMPLETED;
        this.endTime = endAt != null ? endAt : LocalDateTime.now();
    }

    public void autoCloseStale(LocalDateTime endAt) {
        if (!IN_PROGRESS.equals(this.visitStatus)) {
            throw new IllegalStateException("진료 중만 자동 마감 가능");
        }
        this.visitStatus = AUTO_CLOSED;
        this.endTime = endAt != null ? endAt : LocalDateTime.now();
    }

    public void applyAdministrativeVisitStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException();
        }
        String u = raw.trim().toUpperCase();
        if (!ADMIN_STATUSES.contains(u)) {
            throw new IllegalArgumentException();
        }
        this.visitStatus = u;
        LocalDateTime now = LocalDateTime.now();
        if (IN_PROGRESS.equals(u) && this.startTime == null) {
            this.startTime = now;
        }
        if (COMPLETED.equals(u) || AUTO_CLOSED.equals(u)) {
            if (this.startTime == null) {
                this.startTime = now;
            }
            if (this.endTime == null) {
                this.endTime = now;
            }
        }
    }

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getVisitId() { return visitId; }
    public Long getPatientId() { return patientId; }
    public String getDoctorId() { return doctorId; }
    public Long getReceptionId() { return receptionId; }
    public String getVisitStatus() { return visitStatus; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
