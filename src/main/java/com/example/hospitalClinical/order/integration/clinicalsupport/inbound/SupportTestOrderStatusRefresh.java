package com.example.hospitalClinical.order.integration.clinicalsupport.inbound;

import com.example.hospitalClinical.common.client.external.clinicalsupport.ClinicalSupportApiProperties;
import com.example.hospitalClinical.common.client.external.clinicalsupport.ClinicalSupportTestExecutionClient;
import com.example.hospitalClinical.common.client.external.clinicalsupport.ClinicalSupportTestExecutionRow;
import com.example.hospitalClinical.common.exception.BusinessException;
import com.example.hospitalClinical.encounter.repository.VisitRepo;
import com.example.hospitalClinical.order.entity.Order;
import com.example.hospitalClinical.order.entity.OrderItem;
import com.example.hospitalClinical.order.repository.OrderRepo;
import com.example.hospitalClinical.order.service.OrderVisitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SupportTestOrderStatusRefresh {

    private final ClinicalSupportApiProperties apiProperties;
    private final ClinicalSupportTestExecutionClient testExecutionClient;
    private final OrderVisitService orderVisitService;
    private final OrderRepo orderRepo;
    private final VisitRepo visitRepo;

    public SupportTestOrderStatusRefresh(
            ClinicalSupportApiProperties apiProperties,
            ClinicalSupportTestExecutionClient testExecutionClient,
            @Lazy OrderVisitService orderVisitService,
            OrderRepo orderRepo,
            VisitRepo visitRepo) {
        this.apiProperties = apiProperties;
        this.testExecutionClient = testExecutionClient;
        this.orderVisitService = orderVisitService;
        this.orderRepo = orderRepo;
        this.visitRepo = visitRepo;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    public void refreshForVisit(Long visitId, String orderTypeFilterOrNull) {
        if (!apiProperties.isEnabled()) {
            return;
        }
        String f = orderTypeFilterOrNull == null ? "" : orderTypeFilterOrNull.trim();
        if ("PRESCRIPTION".equalsIgnoreCase(f) || "TREATMENT".equalsIgnoreCase(f)) {
            return;
        }
        if (!visitRepo.existsById(visitId)) {
            return;
        }
        List<Order> orders = orderRepo.findByVisitIdOrderByOrderDateDesc(visitId);
        boolean needs =
                orders.stream()
                        .anyMatch(
                                o ->
                                        o.getOrderType() != null
                                                && o.getOrderType().isTestCategory()
                                                && !isCancelledForList(o.getOrderStatus())
                                                && !"COMPLETED"
                                                        .equalsIgnoreCase(
                                                                o.getOrderStatus() == null
                                                                        ? ""
                                                                        : o.getOrderStatus().trim()));
        if (!needs) {
            return;
        }
        List<ClinicalSupportTestExecutionRow> rows;
        try {
            rows = testExecutionClient.fetchAllExecutions();
        } catch (Exception e) {
            log.warn("clinical-support testExecution API failed visitId={} msg={}", visitId, e.getMessage());
            return;
        }
        Map<Long, Integer> rankByOrderItem = maxRankByOrderItemId(rows);
        for (Order o : orders) {
            if (o.getOrderType() == null || !o.getOrderType().isTestCategory()) {
                continue;
            }
            if (isCancelledForList(o.getOrderStatus())) {
                continue;
            }
            String desired = desiredTestOrderStatus(o, rankByOrderItem);
            if (desired == null) {
                continue;
            }
            String cur = o.getOrderStatus() == null ? "" : o.getOrderStatus().trim();
            if (desired.equalsIgnoreCase(cur)) {
                continue;
            }
            try {
                orderVisitService.syncOrderStatusFromSupport(visitId, o.getOrderId(), desired);
            } catch (BusinessException ex) {
                log.warn(
                        "test order status sync visitId={} orderId={} desired={} msg={}",
                        visitId,
                        o.getOrderId(),
                        desired,
                        ex.getMessage());
            }
        }
    }

    private static boolean isCancelledForList(String orderStatus) {
        if (orderStatus == null || orderStatus.isBlank()) {
            return false;
        }
        return "CANCELLED".equalsIgnoreCase(orderStatus.trim());
    }

    private static Map<Long, Integer> maxRankByOrderItemId(List<ClinicalSupportTestExecutionRow> rows) {
        Map<Long, Integer> map = new HashMap<>();
        for (ClinicalSupportTestExecutionRow row : rows) {
            if (row == null || row.getOrderItemId() == null) {
                continue;
            }
            int r = progressRank(row.getProgressStatus());
            if (r <= 0) {
                continue;
            }
            map.merge(row.getOrderItemId(), r, Math::max);
        }
        return map;
    }

    private static int progressRank(String progressStatus) {
        if (progressStatus == null || progressStatus.isBlank()) {
            return 0;
        }
        String u = progressStatus.trim().toUpperCase();
        if ("COMPLETED".equals(u) || "DONE".equals(u) || "COMPLETE".equals(u) || "FINISHED".equals(u)) {
            return 3;
        }
        if ("IN_PROGRESS".equals(u)
                || "INPROGRESS".equals(u)
                || "RUNNING".equals(u)
                || "EXECUTING".equals(u)
                || "WORKING".equals(u)) {
            return 2;
        }
        if ("WAITING".equals(u) || "REQUESTED".equals(u) || "REQUEST".equals(u) || "PENDING".equals(u)) {
            return 1;
        }
        return 0;
    }

    private static String desiredTestOrderStatus(Order order, Map<Long, Integer> rankByOrderItem) {
        List<OrderItem> items = order.getItems();
        if (items == null || items.isEmpty()) {
            return null;
        }
        List<OrderItem> withIds =
                items.stream().filter(it -> it.getOrderItemId() != null).toList();
        if (withIds.isEmpty()) {
            return null;
        }
        boolean allCompleted =
                withIds.stream()
                        .allMatch(it -> rankByOrderItem.getOrDefault(it.getOrderItemId(), 0) >= 3);
        if (allCompleted) {
            return "COMPLETED";
        }
        boolean anyProgressOrDone =
                withIds.stream()
                        .map(it -> rankByOrderItem.getOrDefault(it.getOrderItemId(), 0))
                        .anyMatch(r -> r >= 2);
        boolean anyCompletedLine =
                withIds.stream()
                        .map(it -> rankByOrderItem.getOrDefault(it.getOrderItemId(), 0))
                        .anyMatch(r -> r >= 3);
        if (anyProgressOrDone || anyCompletedLine) {
            return "IN_PROGRESS";
        }
        return null;
    }
}
