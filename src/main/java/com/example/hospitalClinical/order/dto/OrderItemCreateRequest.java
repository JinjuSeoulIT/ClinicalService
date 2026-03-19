package com.example.hospitalClinical.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemCreateRequest {
    @NotBlank
    @Size(max = 50)
    private String itemCode;
    private BigDecimal dose;
    @Size(max = 100)
    private String frequency;
    @Size(max = 100)
    private String duration;
}
