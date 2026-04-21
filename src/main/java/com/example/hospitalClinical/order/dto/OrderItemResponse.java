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
        PrescriptionLineFields rx = decodePrescriptionLineFields(raw);
        r.setItemDetailCode(rx.displayName());
        r.setItemName(rx.displayName());
        r.setDosage(rx.dosage());
        r.setDose(null);
        r.setFrequency(rx.frequency());
        r.setDuration(rx.duration());
        r.setCreatedAt(i.getCreatedAt());
        return r;
    }

    public record PrescriptionLineFields(
            String displayName, String dosage, String frequency, String duration) {}

    public static PrescriptionLineFields decodePrescriptionLineFields(String raw) {
        if (raw == null || raw.isEmpty()) {
            return new PrescriptionLineFields(null, null, null, null);
        }
        int sep = raw.indexOf('\u001e');
        if (sep < 0) {
            String only = stripEncodedOrderItemSuffix(raw);
            return new PrescriptionLineFields(emptyToNull(only), null, null, null);
        }
        String namePart = stripEncodedOrderItemSuffix(raw.substring(0, sep).trim());
        String tail = raw.substring(sep + 1);
        String[] parts = tail.split("\u001f", -1);
        String d0 = parts.length > 0 ? emptyToNull(parts[0]) : null;
        String d1 = parts.length > 1 ? emptyToNull(parts[1]) : null;
        String d2 = parts.length > 2 ? emptyToNull(parts[2]) : null;
        return new PrescriptionLineFields(emptyToNull(namePart), d0, d1, d2);
    }

    public static String encodePrescriptionLineFields(
            String displayName, String dosage, String frequency, String duration) {
        String name = stripEncodedOrderItemSuffix(displayName == null ? "" : displayName.trim());
        if (name == null) {
            name = "";
        }
        boolean any =
                (dosage != null && !dosage.isBlank())
                        || (frequency != null && !frequency.isBlank())
                        || (duration != null && !duration.isBlank());
        if (!any) {
            return name.isEmpty() ? null : name;
        }
        return name
                + '\u001e'
                + (dosage == null ? "" : dosage.trim())
                + '\u001f'
                + (frequency == null ? "" : frequency.trim())
                + '\u001f'
                + (duration == null ? "" : duration.trim());
    }

    private static String emptyToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
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
