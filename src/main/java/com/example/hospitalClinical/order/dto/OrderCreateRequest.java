package com.example.hospitalClinical.order.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderCreateRequest {
    private String orderType;
    private Long doctorId;
    private List<OrderItemCreateRequest> items;
}
