package com.example.hospitalClinical.order.repository;

import com.example.hospitalClinical.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepo extends JpaRepository<Order, Long> {

    @Query(
            "SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM OrderHeader o WHERE"
                    + " o.legacyPrescriptionId = :legacyPrescriptionId")
    boolean existsByLegacyPrescriptionId(@Param("legacyPrescriptionId") Long legacyPrescriptionId);

    @Query("SELECT DISTINCT o FROM OrderHeader o LEFT JOIN FETCH o.items WHERE o.visitId = :visitId ORDER BY o.orderDate DESC")
    List<Order> findByVisitIdOrderByOrderDateDesc(@Param("visitId") Long visitId);

    @Query("SELECT o FROM OrderHeader o LEFT JOIN FETCH o.items WHERE o.orderId = :orderId")
    Optional<Order> findByIdWithItems(@Param("orderId") Long orderId);
}
