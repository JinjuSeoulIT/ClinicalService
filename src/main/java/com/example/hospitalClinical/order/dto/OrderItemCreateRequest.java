package com.example.hospitalClinical.order.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemCreateRequest {
    @Size(max = 50)
    private String itemCode;
    @Size(max = 600)
    private String itemName;
    @Size(max = 200)
    private String dosage;
    private BigDecimal dose;
    @Size(max = 100)
    private String frequency;
    @Size(max = 100)
    private String duration;
}
