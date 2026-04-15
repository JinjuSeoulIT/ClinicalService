package com.example.hospitalClinical.order.repository;

import com.example.hospitalClinical.order.entity.MedicationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MedicationRecordRepo extends JpaRepository<MedicationRecord, String> {

    List<MedicationRecord> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    @Query(
            """
            select m from MedicationRecord m
            where m.medicationId = (
                select max(m2.medicationId) from MedicationRecord m2
                where m2.medicationId like concat(:prefix, '%')
            )
            """)
    Optional<MedicationRecord> findLatestByMedicationIdPrefix(@Param("prefix") String prefix);
}
