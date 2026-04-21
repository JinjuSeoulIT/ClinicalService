package com.example.hospitalClinical.order.integration.clinicalsupport.inbound;

import com.example.hospitalClinical.common.exception.BusinessException;
import com.example.hospitalClinical.order.entity.Order;
import com.example.hospitalClinical.order.entity.OrderItem;
import com.example.hospitalClinical.order.repository.MedicationRecordRepo;
import com.example.hospitalClinical.order.repository.OrderItemRepo;
import com.example.hospitalClinical.order.repository.OrderRepo;
import com.example.hospitalClinical.order.repository.TreatmentResultRepo;
import com.example.hospitalClinical.order.service.OrderVisitService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupportOrderFeedbackProcessor {

    private final ObjectMapper objectMapper;
    private final OrderVisitService orderVisitService;
    private final OrderItemRepo orderItemRepo;
    private final OrderRepo orderRepo;
    private final MedicationRecordRepo medicationRecordRepo;
    private final TreatmentResultRepo treatmentResultRepo;
    private final LabOrderCompletionAggregator labOrderCompletionAggregator;

    @Transactional
    public void onMedicationMessage(String raw) {
        try {
            JsonNode root = objectMapper.readTree(raw);
            JsonNode data = dataNode(root);
            if (tryDirectOrderStatusFromPayload(data, root)) {
                return;
            }
            if (shouldSkipOutboundRegistration(data, eventType(root))) {
                return;
            }
            Progress p = readProgress(data, root);
            if (p != Progress.COMPLETED) {
                return;
            }
            String medicationId = readText(data, root, "medicationId", "medication_id");
            if (medicationId == null || medicationId.isBlank()) {
                return;
            }
            medicationRecordRepo
                    .findById(medicationId.trim())
                    .ifPresent(
                            m -> {
                                m.setStatus("COMPLETED");
                                medicationRecordRepo.save(m);
                            });
        } catch (Exception e) {
            log.warn("support-feedback medication parse/handle failed: {}", e.getMessage());
        }
    }

    @Transactional
    public void onTreatmentMessage(String raw) {
        try {
            JsonNode root = objectMapper.readTree(raw);
            JsonNode data = dataNode(root);
            if (tryDirectOrderStatusFromPayload(data, root)) {
                return;
            }
            if (shouldSkipOutboundRegistration(data, eventType(root))) {
                return;
            }
            Progress p = readProgress(data, root);
            if (p != Progress.COMPLETED) {
                return;
            }
            String procedureResultId = readText(data, root, "procedureResultId", "procedure_result_id");
            if (procedureResultId == null || procedureResultId.isBlank()) {
                return;
            }
            treatmentResultRepo
                    .findById(procedureResultId.trim())
                    .ifPresent(
                            t -> {
                                t.setStatus("COMPLETED");
                                treatmentResultRepo.save(t);
                            });
        } catch (Exception e) {
            log.warn("support-feedback treatment parse/handle failed: {}", e.getMessage());
        }
    }

    @Transactional
    public void onTestExecutionMessage(String raw) {
        try {
            JsonNode root = objectMapper.readTree(raw);
            JsonNode data = dataNode(root);
            String ev = eventType(root);
            if (tryDirectOrderStatusFromPayload(data, root)) {
                return;
            }
            if (shouldSkipOutboundRegistration(data, ev)) {
                return;
            }
            Progress p = readProgress(data, root);
            if (p == Progress.NONE) {
                return;
            }
            Long orderItemId = readLong(data, root, "orderItemId", "order_item_id", "ORDER_ITEM_ID");
            if (orderItemId == null && root.get("key") != null && root.get("key").canConvertToLong()) {
                orderItemId = root.get("key").longValue();
            }
            if (orderItemId == null) {
                return;
            }
            Optional<OrderItem> itemOpt = orderItemRepo.findWithOrderByOrderItemId(orderItemId);
            if (itemOpt.isEmpty()) {
                return;
            }
            OrderItem item = itemOpt.get();
            Order order = item.getOrder();
            if (order == null) {
                return;
            }
            Order full =
                    orderRepo.findByIdWithItems(order.getOrderId()).orElse(order);
            Long visit = full.getVisitId();
            Long oid = full.getOrderId();
            int lines = full.getItems() == null ? 0 : full.getItems().size();
            if (lines <= 0) {
                return;
            }
            if (p == Progress.IN_PROGRESS) {
                safeSync(visit, oid, "IN_PROGRESS");
                return;
            }
            if (lines == 1) {
                safeSync(visit, oid, "COMPLETED");
                labOrderCompletionAggregator.clearOrder(oid);
                return;
            }
            labOrderCompletionAggregator.recordLineCompleted(oid, orderItemId);
            if (labOrderCompletionAggregator.isAllLinesCompleted(oid, lines)) {
                safeSync(visit, oid, "COMPLETED");
                labOrderCompletionAggregator.clearOrder(oid);
            } else {
                safeSync(visit, oid, "IN_PROGRESS");
            }
        } catch (Exception e) {
            log.warn("support-feedback testExecution parse/handle failed: {}", e.getMessage());
        }
    }

    private boolean tryDirectOrderStatusFromPayload(JsonNode data, JsonNode root) {
        Long visitId = readLong(data, root, "visitId", "visit_id");
        Long orderId = readLong(data, root, "orderId", "order_id");
        String orderStatus = readText(data, root, "orderStatus", "order_status");
        if (visitId != null && orderId != null && orderStatus != null && !orderStatus.isBlank()) {
            applyDirectOrderStatus(visitId, orderId, orderStatus);
            return true;
        }
        return false;
    }

    private void applyDirectOrderStatus(Long visitId, Long orderId, String orderStatus) {
        String u = orderStatus.trim().toUpperCase();
        if ("REQUEST".equals(u)) {
            u = "REQUESTED";
        }
        if ("COMPLETED".equals(u) || "IN_PROGRESS".equals(u) || "CANCELLED".equals(u) || "REQUESTED".equals(u)) {
            safeSync(visitId, orderId, u);
            if ("COMPLETED".equals(u)) {
                labOrderCompletionAggregator.clearOrder(orderId);
            }
        }
    }

    private void safeSync(Long visitId, Long orderId, String status) {
        try {
            orderVisitService.syncOrderStatusFromSupport(visitId, orderId, status);
        } catch (BusinessException ex) {
            log.warn(
                    "support-feedback syncOrderStatusFromSupport visitId={} orderId={} status={} msg={}",
                    visitId,
                    orderId,
                    status,
                    ex.getMessage());
        }
    }

    private static JsonNode dataNode(JsonNode root) {
        if (root == null || !root.isObject()) {
            return root;
        }
        JsonNode data = root.get("data");
        if (data != null && data.isObject()) {
            return data;
        }
        return root;
    }

    private static String eventType(JsonNode root) {
        if (root == null || !root.isObject()) {
            return null;
        }
        JsonNode t = root.get("eventType");
        if (t == null || t.isNull()) {
            return null;
        }
        return t.asText();
    }

    private static boolean shouldSkipOutboundRegistration(JsonNode data, String eventType) {
        if (data == null || !data.isObject()) {
            return false;
        }
        if (eventType == null || !"CREATE".equalsIgnoreCase(eventType)) {
            return false;
        }
        String ps =
                textOrEmpty(
                        data,
                        "progressStatus",
                        "progress_status",
                        "PROGRESS_STATUS",
                        "orderStatus",
                        "order_status",
                        "status");
        String u = ps.trim().toUpperCase();
        return u.isEmpty()
                || "WAITING".equals(u)
                || "REQUESTED".equals(u)
                || "REQUEST".equals(u)
                || "PENDING".equals(u);
    }

    private enum Progress {
        NONE,
        IN_PROGRESS,
        COMPLETED
    }

    private static Progress readProgress(JsonNode data, JsonNode root) {
        String raw =
                textOrEmpty(
                        data,
                        "progressStatus",
                        "progress_status",
                        "PROGRESS_STATUS",
                        "orderStatus",
                        "order_status",
                        "status");
        if (root != null && root.isObject() && raw.isEmpty()) {
            raw =
                    textOrEmpty(
                            root,
                            "progressStatus",
                            "progress_status",
                            "PROGRESS_STATUS",
                            "orderStatus",
                            "order_status",
                            "status");
        }
        String u = raw.trim().toUpperCase();
        if (u.isEmpty()) {
            return Progress.NONE;
        }
        if ("COMPLETED".equals(u) || "COMPLETE".equals(u) || "DONE".equals(u) || "FINISHED".equals(u)) {
            return Progress.COMPLETED;
        }
        if ("IN_PROGRESS".equals(u)
                || "INPROGRESS".equals(u)
                || "RUNNING".equals(u)
                || "EXECUTING".equals(u)
                || "WORKING".equals(u)) {
            return Progress.IN_PROGRESS;
        }
        return Progress.NONE;
    }

    private static String readText(JsonNode data, JsonNode root, String... names) {
        String v = readText(data, names);
        if (v != null && !v.isBlank()) {
            return v;
        }
        return readText(root, names);
    }

    private static String readText(JsonNode node, String... names) {
        if (node == null || !node.isObject()) {
            return null;
        }
        for (String name : names) {
            JsonNode v = node.get(name);
            if (v == null || v.isNull()) {
                continue;
            }
            if (v.isTextual()) {
                String t = v.asText().trim();
                if (!t.isEmpty()) {
                    return t;
                }
            }
            if (v.isIntegralNumber()) {
                return String.valueOf(v.longValue());
            }
        }
        return null;
    }

    private static Long readLong(JsonNode data, JsonNode root, String... names) {
        Long v = readLong(data, names);
        if (v != null) {
            return v;
        }
        return readLong(root, names);
    }

    private static Long readLong(JsonNode node, String... names) {
        if (node == null || !node.isObject()) {
            return null;
        }
        for (String name : names) {
            JsonNode v = node.get(name);
            if (v == null || v.isNull()) {
                continue;
            }
            if (v.isIntegralNumber()) {
                return v.longValue();
            }
            if (v.isTextual()) {
                String t = v.asText().trim();
                if (t.isEmpty()) {
                    continue;
                }
                try {
                    return Long.parseLong(t);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private static String textOrEmpty(JsonNode node, String... names) {
        String s = readText(node, names);
        return s == null ? "" : s;
    }
}
