package com.example.hospitalClinical.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class VisitOrderCreateRequest {

    @NotNull
    private Long visitId;

    private String orderType;

    private String doctorId;

    @NotEmpty
    @Valid
    private List<OrderItemCreateRequest> items;
}
