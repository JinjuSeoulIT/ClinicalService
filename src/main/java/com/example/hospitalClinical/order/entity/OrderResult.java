package com.example.hospitalClinical.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "CLINICAL_ORDER_RESULT")
public class OrderResult {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_result_seq_gen")
    @SequenceGenerator(name = "order_result_seq_gen", sequenceName = "CL_ORDER_RESULT_SEQ", allocationSize = 1)
    @Column(name = "RESULT_ID", nullable = false)
    private Long resultId;

    @Column(name = "ORDER_ITEM_ID", nullable = false)
    private Long orderItemId;

    @Column(name = "RESULT_VALUE", length = 2000)
    private String resultValue;

    @Column(name = "RESULT_STATUS", length = 20)
    private String resultStatus;

    @Column(name = "RESULT_DATE")
    private LocalDateTime resultDate;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    protected OrderResult() {}

    public static OrderResult create(Long orderItemId, String resultValue, String resultStatus) {
        OrderResult r = new OrderResult();
        r.orderItemId = orderItemId;
        r.resultValue = resultValue;
        r.resultStatus = resultStatus != null ? resultStatus : "PENDING";
        r.resultDate = LocalDateTime.now();
        return r;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (resultDate == null) resultDate = LocalDateTime.now();
    }

    public Long getResultId() { return resultId; }
    public Long getOrderItemId() { return orderItemId; }
    public String getResultValue() { return resultValue; }
    public void setResultValue(String resultValue) { this.resultValue = resultValue; }
    public String getResultStatus() { return resultStatus; }
    public void setResultStatus(String resultStatus) { this.resultStatus = resultStatus; }
    public LocalDateTime getResultDate() { return resultDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
