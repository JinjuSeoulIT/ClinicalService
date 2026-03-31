package com.example.hospitalClinical.order.dto;

import com.example.hospitalClinical.order.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private Long orderItemId;
    private Long orderId;
    private String itemCode;
    private String itemName;
    private String dosage;
    private BigDecimal dose;
    private String frequency;
    private String duration;
    private LocalDateTime createdAt;

    public static OrderItemResponse from(OrderItem i) {
        return new OrderItemResponse(
                i.getOrderItemId(),
                i.getOrder() != null ? i.getOrder().getOrderId() : null,
                i.getItemCode(),
                i.getItemName(),
                i.getItemDosage(),
                i.getDose(),
                i.getFrequency(),
                i.getDuration(),
                i.getCreatedAt()
        );
    }
}
