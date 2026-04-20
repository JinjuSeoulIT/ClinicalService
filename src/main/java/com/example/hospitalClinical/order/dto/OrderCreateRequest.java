package com.example.hospitalClinical.order.dto;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateRequest {
    private String orderType;
    private String doctorId;

    @Valid
    private List<OrderItemCreateRequest> items;
}
