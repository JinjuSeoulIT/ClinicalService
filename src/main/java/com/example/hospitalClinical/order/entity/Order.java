package com.example.hospitalClinical.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "OrderHeader")
@Table(name = "CLINICAL_ORDER")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "medical_order_seq_gen")
    @SequenceGenerator(name = "medical_order_seq_gen", sequenceName = "CL_ORDER_SEQ", allocationSize = 1)
    @Column(name = "ORDER_ID", nullable = false)
    private Long orderId;

    @Column(name = "VISIT_ID", nullable = false)
    private Long visitId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ORDER_TYPE", length = 20)
    private OrderType orderType;

    @Column(name = "ORDER_STATUS", length = 20)
    private String orderStatus;

    @Column(name = "DOCTOR_ID", length = 30)
    private String doctorId;

    @Column(name = "ORDER_DATE")
    private LocalDateTime orderDate;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "LEGACY_PRESCRIPTION_ID")
    private Long legacyPrescriptionId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<OrderItem> items = new ArrayList<>();

    protected Order() {}

    public static Order create(Long visitId, OrderType orderType, String orderStatus, String doctorId) {
        Order o = new Order();
        o.visitId = visitId;
        o.orderType = orderType;
        o.orderStatus = orderStatus != null ? orderStatus : "REQUESTED";
        o.doctorId = doctorId;
        o.orderDate = LocalDateTime.now();
        return o;
    }

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (orderDate == null) orderDate = now;
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void setLegacyPrescriptionId(Long legacyPrescriptionId) {
        this.legacyPrescriptionId = legacyPrescriptionId;
    }

    public Long getLegacyPrescriptionId() {
        return legacyPrescriptionId;
    }

    public Long getOrderId() { return orderId; }
    public Long getVisitId() { return visitId; }
    public OrderType getOrderType() { return orderType; }
    public String getOrderStatus() { return orderStatus; }
    public String getDoctorId() { return doctorId; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<OrderItem> getItems() { return items; }
}
