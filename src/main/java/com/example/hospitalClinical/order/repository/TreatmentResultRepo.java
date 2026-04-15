package com.example.hospitalClinical.order.repository;

import com.example.hospitalClinical.order.entity.TreatmentResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TreatmentResultRepo extends JpaRepository<TreatmentResult, String> {

    List<TreatmentResult> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    @Query(
            """
            select t from TreatmentResult t
            where t.procedureResultId = (
                select max(t2.procedureResultId) from TreatmentResult t2
                where t2.procedureResultId like concat(:prefix, '%')
            )
            """)
    Optional<TreatmentResult> findLatestByProcedureResultIdPrefix(@Param("prefix") String prefix);
}
