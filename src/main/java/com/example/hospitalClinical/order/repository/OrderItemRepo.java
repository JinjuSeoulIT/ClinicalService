package com.example.hospitalClinical.order.repository;

import com.example.hospitalClinical.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderItemRepo extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrder_OrderId(Long orderId);

    @Query("select i from OrderItem i join fetch i.order where i.orderItemId = :orderItemId")
    Optional<OrderItem> findWithOrderByOrderItemId(@Param("orderItemId") Long orderItemId);
}
