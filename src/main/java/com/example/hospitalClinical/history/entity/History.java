package com.example.hospitalClinical.history.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "CLINICAL_HISTORY")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "history_seq")
    @SequenceGenerator(
            name = "history_seq",
            sequenceName = "CLINICAL_HISTORY_SEQ",
            allocationSize = 1
    )
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "PATIENT_ID", nullable = false)
    private Long patientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "HISTORY_TYPE", length = 20, nullable = false)
    private HistoryType historyType;

    @Column(name = "NAME", length = 100, nullable = false)
    private String name;

    @Column(name = "MEMO", length = 500)
    private String memo;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

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
}
