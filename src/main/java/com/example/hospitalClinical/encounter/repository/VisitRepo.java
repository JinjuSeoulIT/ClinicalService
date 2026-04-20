package com.example.hospitalClinical.encounter.repository;

import com.example.hospitalClinical.encounter.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface VisitRepo extends JpaRepository<Visit, Long> {

    List<Visit> findByPatientIdOrderByStartTimeDesc(Long patientId);
    List<Visit> findByReceptionIdOrderByStartTimeDesc(Long receptionId);
    List<Visit> findByReceptionIdAndVisitStatus(Long receptionId, String visitStatus);
    List<Visit> findByVisitStatusAndDoctorId(String visitStatus, String doctorId);
    List<Visit> findByVisitStatusOrderByStartTimeAsc(String visitStatus);

    List<Visit> findByVisitStatusInOrderByStartTimeAsc(Collection<String> visitStatuses);

    List<Visit> findAllByOrderByStartTimeDesc();

    @Query(
            "select v from Visit v where v.visitStatus = :status and ("
                    + "(v.startTime is not null and v.startTime < :before) or "
                    + "(v.startTime is null and v.createdAt < :before))"
    )
    List<Visit> findStaleInProgress(@Param("status") String status, @Param("before") LocalDateTime before);
}
