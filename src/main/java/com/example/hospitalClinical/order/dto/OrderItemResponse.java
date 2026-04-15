package com.example.hospitalClinical.order.dto;

import com.example.hospitalClinical.order.entity.OrderItem;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class OrderItemResponse {
    private Long orderItemId;
    private Long orderId;
    private String itemCode;
    private String itemDetailCode;
    private String itemName;
    private String dosage;
    private java.math.BigDecimal dose;
    private String frequency;
    private String duration;
    private LocalDateTime createdAt;

    public static OrderItemResponse from(OrderItem i) {
        OrderItemResponse r = new OrderItemResponse();
        r.setOrderItemId(i.getOrderItemId());
        r.setOrderId(i.getOrder() != null ? i.getOrder().getOrderId() : null);
        r.setItemCode(i.getItemCode());
        String d = i.getItemDetailCode();
        r.setItemDetailCode(d);
        r.setItemName(d);
        r.setDosage(null);
        r.setDose(null);
        r.setFrequency(null);
        r.setDuration(null);
        r.setCreatedAt(i.getCreatedAt());
        return r;
    }
}
