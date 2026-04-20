package com.example.hospitalClinical.order.dto;

import com.example.hospitalClinical.order.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long orderId;
    private Long visitId;
    private String orderType;
    private String orderStatus;
    private String doctorId;
    private LocalDateTime orderDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemResponse> items;

    public static OrderResponse from(Order o) {
        List<OrderItemResponse> itemList = o.getItems().stream().map(OrderItemResponse::from).collect(Collectors.toList());
        return new OrderResponse(
                o.getOrderId(),
                o.getVisitId(),
                o.getOrderType() != null ? o.getOrderType().name() : null,
                o.getOrderStatus(),
                o.getDoctorId(), o.getOrderDate(), o.getCreatedAt(), o.getUpdatedAt(), itemList
        );
    }
}
