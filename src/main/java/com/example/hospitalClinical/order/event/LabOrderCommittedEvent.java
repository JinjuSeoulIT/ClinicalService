package com.example.hospitalClinical.order.event;

import com.example.hospitalClinical.order.entity.OrderType;

import java.util.List;

public record LabOrderCommittedEvent(OrderType orderType, List<Long> orderItemIds) {}
