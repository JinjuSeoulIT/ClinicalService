package com.example.hospitalClinical.order.integration.clinicalsupport.inbound;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LabOrderCompletionAggregator {

    private final ConcurrentHashMap<Long, Set<Long>> completedItemIdsByOrderId = new ConcurrentHashMap<>();

    public void recordLineCompleted(Long orderId, Long orderItemId) {
        if (orderId == null || orderItemId == null) {
            return;
        }
        completedItemIdsByOrderId
                .computeIfAbsent(orderId, k -> ConcurrentHashMap.newKeySet())
                .add(orderItemId);
    }

    public boolean isAllLinesCompleted(Long orderId, int expectedLineCount) {
        if (orderId == null || expectedLineCount <= 0) {
            return false;
        }
        Set<Long> done = completedItemIdsByOrderId.get(orderId);
        return done != null && done.size() >= expectedLineCount;
    }

    public void clearOrder(Long orderId) {
        if (orderId != null) {
            completedItemIdsByOrderId.remove(orderId);
        }
    }
}
