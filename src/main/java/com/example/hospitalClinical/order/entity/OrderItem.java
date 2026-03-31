package com.example.hospitalClinical.order.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "CLINICAL_ORDER_ITEM")
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

    @Column(name = "ITEM_NAME", length = 600)
    private String itemName;

    @Column(name = "ITEM_DOSAGE", length = 200)
    private String itemDosage;

    @Column(name = "DOSE", precision = 10, scale = 2)
    private BigDecimal dose;

    @Column(name = "FREQUENCY", length = 100)
    private String frequency;

    @Column(name = "DURATION", length = 100)
    private String duration;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    protected OrderItem() {}

    public static OrderItem create(String itemCode, BigDecimal dose, String frequency, String duration) {
        OrderItem i = new OrderItem();
        i.itemCode = itemCode;
        i.dose = dose;
        i.frequency = frequency;
        i.duration = duration;
        return i;
    }

    public static OrderItem createPrescriptionLine(
            String itemName, String itemDosage, String frequency, String duration) {
        OrderItem i = new OrderItem();
        i.itemName = itemName;
        i.itemDosage = itemDosage;
        i.frequency = frequency;
        i.duration = duration;
        return i;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    void setOrder(Order order) {
        this.order = order;
    }

    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setItemDosage(String itemDosage) { this.itemDosage = itemDosage; }
    public void setDose(BigDecimal dose) { this.dose = dose; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public void setDuration(String duration) { this.duration = duration; }

    public Long getOrderItemId() { return orderItemId; }
    public Order getOrder() { return order; }
    public String getItemCode() { return itemCode; }
    public String getItemName() { return itemName; }
    public String getItemDosage() { return itemDosage; }
    public BigDecimal getDose() { return dose; }
    public String getFrequency() { return frequency; }
    public String getDuration() { return duration; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
