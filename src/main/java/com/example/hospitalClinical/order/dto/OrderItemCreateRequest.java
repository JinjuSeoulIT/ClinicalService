package com.example.hospitalClinical.order.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OrderItemCreateRequest {
    @Size(max = 50)
    private String itemCode;

    @Size(max = 600)
    private String itemDetailCode;

    @Size(max = 600)
    private String itemName;

    @Size(max = 200)
    private String dosage;

    private java.math.BigDecimal dose;

    @Size(max = 100)
    private String frequency;

    @Size(max = 100)
    private String duration;
}
