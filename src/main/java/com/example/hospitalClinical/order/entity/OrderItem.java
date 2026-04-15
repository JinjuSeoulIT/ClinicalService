package com.example.hospitalClinical.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "CLINICAL_LAB_ORDER_ITEM")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_item_seq_gen")
    @SequenceGenerator(name = "order_item_seq_gen", sequenceName = "CL_ORDER_ITEM_SEQ", allocationSize = 1)
    @Column(name = "ORDER_ITEM_ID", nullable = false)
    private Long orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORDER_ID", nullable = false)
    private Order order;

    @Column(name = "ITEM_CODE", length = 50)
    private String itemCode;

    @Column(name = "ITEM_DETAIL_CODE", length = 600)
    private String itemDetailCode;

    @Column(name = "PATIENT_ID", nullable = false)
    private Long patientId;

    @Column(name = "PATIENT_NAME", length = 200)
    private String patientName;

    @Column(name = "DEPARTMENT_NAME", length = 200)
    private String departmentName;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    protected OrderItem() {}

    public static OrderItem createLabLine(
            String itemCode,
            String itemDetailCode,
            Long patientId,
            String patientName,
            String departmentName) {
        OrderItem i = new OrderItem();
        i.itemCode = emptyToNull(itemCode);
        i.itemDetailCode = emptyToNull(itemDetailCode);
        i.patientId = patientId;
        i.patientName = emptyToNull(patientName);
        i.departmentName = emptyToNull(departmentName);
        return i;
    }

    public static OrderItem createPrescriptionLine(
            String itemDetailCode, Long patientId, String patientName, String departmentName) {
        return createLabLine(null, itemDetailCode, patientId, patientName, departmentName);
    }

    private static String emptyToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    void setOrder(Order order) {
        this.order = order;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = emptyToNull(itemCode);
    }

    public void setItemDetailCode(String itemDetailCode) {
        this.itemDetailCode = emptyToNull(itemDetailCode);
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public void setPatientName(String patientName) {
        this.patientName = emptyToNull(patientName);
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = emptyToNull(departmentName);
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    public Order getOrder() {
        return order;
    }

    public String getItemCode() {
        return itemCode;
    }

    public String getItemDetailCode() {
        return itemDetailCode;
    }

    public Long getPatientId() {
        return patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
