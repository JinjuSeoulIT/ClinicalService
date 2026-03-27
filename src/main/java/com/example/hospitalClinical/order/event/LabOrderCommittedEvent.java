package com.example.hospitalClinical.order.event;

import java.util.List;

public record LabOrderCommittedEvent(String orderType, List<Long> orderItemIds, Long doctorId) {
}
