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
        String raw = i.getItemDetailCode();
        String d = stripEncodedOrderItemSuffix(raw);
        r.setItemDetailCode(d);
        r.setItemName(d);
        r.setDosage(null);
        r.setDose(null);
        r.setFrequency(null);
        r.setDuration(null);
        r.setCreatedAt(i.getCreatedAt());
        return r;
    }

    public static String stripEncodedOrderItemSuffix(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        int cut = s.length();
        for (char sep : new char[] {'\u001e', '\u001f'}) {
            int i = s.indexOf(sep);
            if (i >= 0 && i < cut) {
                cut = i;
            }
        }
        if (cut >= s.length()) {
            return s;
        }
        String head = s.substring(0, cut).trim();
        return head.isEmpty() ? s : head;
    }
}
