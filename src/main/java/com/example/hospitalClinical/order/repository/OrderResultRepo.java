package com.example.hospitalClinical.order.repository;

import com.example.hospitalClinical.order.entity.OrderResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderResultRepo extends JpaRepository<OrderResult, Long> {

    List<OrderResult> findByOrderItemIdOrderByResultDateDesc(Long orderItemId);
}
