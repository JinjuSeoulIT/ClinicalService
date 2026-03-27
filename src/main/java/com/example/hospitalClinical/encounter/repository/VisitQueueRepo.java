package com.example.hospitalClinical.encounter.repository;

import com.example.hospitalClinical.encounter.entity.VisitQueue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VisitQueueRepo extends JpaRepository<VisitQueue, Long> {

    List<VisitQueue> findByVisitIdOrderByQueueOrderAsc(Long visitId);
    List<VisitQueue> findAllByOrderByQueueOrderAsc();
}
