package com.hospital.billing.dto;

import com.hospital.billing.entity.Bill;
import com.hospital.billing.entity.BillingStatus;

import java.time.LocalDateTime;

public class BillSummaryResponse {

    private Long billId;
    private Long patientId;
    private LocalDateTime treatmentDate;
    private Integer totalAmount;
    private BillingStatus status;
    private Integer remainingAmount;

    public BillSummaryResponse() {
    }

    // ⭐ Bill 엔티티 기반 생성자 추가
    public BillSummaryResponse(Bill bill) {
        this.billId = bill.getId();
        this.patientId = bill.getPatientId();
        this.treatmentDate = bill.getTreatmentDate().toLocalDateTime();
        this.totalAmount = bill.getTotalAmount();
        this.status = bill.getStatus();
        this.remainingAmount = bill.getRemainingAmount();
    }

    public Long getBillId() {
        return billId;
    }

    public Long getPatientId() {
        return patientId;
    }

    public LocalDateTime getTreatmentDate() {
        return treatmentDate;
    }

    public Integer getTotalAmount() {
        return totalAmount;
    }

    public BillingStatus getStatus() {
        return status;
    }

    public Integer getRemainingAmount() {
        return remainingAmount;
    }
}