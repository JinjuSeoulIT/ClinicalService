package com.example.hospitalClinical.order.service;

import com.example.hospitalClinical.common.exception.BusinessException;
import com.example.hospitalClinical.common.exception.ErrorCode;
import com.example.hospitalClinical.documentation.entity.SoapRx;
import com.example.hospitalClinical.documentation.repository.SoapRxRepo;
import com.example.hospitalClinical.documentation.service.ChartService;
import com.example.hospitalClinical.encounter.exception.VisitNotFoundException;
import com.example.hospitalClinical.encounter.repository.VisitRepo;
import com.example.hospitalClinical.order.dto.OrderCreateRequest;
import com.example.hospitalClinical.order.dto.OrderItemCreateRequest;
import com.example.hospitalClinical.order.dto.OrderItemResponse;
import com.example.hospitalClinical.order.dto.OrderResponse;
import com.example.hospitalClinical.order.entity.Order;
import com.example.hospitalClinical.order.entity.OrderItem;
import com.example.hospitalClinical.order.entity.OrderResult;
import com.example.hospitalClinical.order.entity.OrderType;
import com.example.hospitalClinical.order.event.LabOrderCommittedEvent;
import com.example.hospitalClinical.order.exception.OrderNotFoundException;
import com.example.hospitalClinical.order.repository.OrderItemRepo;
import com.example.hospitalClinical.order.repository.OrderRepo;
import com.example.hospitalClinical.order.repository.OrderResultRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderVisitServiceImpl implements OrderVisitService {

    private final OrderRepo orderRepo;
    private final OrderItemRepo orderItemRepo;
    private final OrderResultRepo orderResultRepo;
    private final VisitRepo visitRepo;
    private final ApplicationEventPublisher eventPublisher;
    private final SoapRxRepo soapRxRepo;
    private final ChartService chartService;

    private static final Set<String> SUPPORT_ALLOWED_ORDER_STATUS =
            Set.of("REQUESTED", "IN_PROGRESS", "COMPLETED", "CANCELLED");

    @Override
    public List<OrderResponse> listOrders(Long visitId, String orderTypeFilterOrNull) {
        String f = orderTypeFilterOrNull == null ? "" : orderTypeFilterOrNull.trim();
        if (f.isEmpty()) {
            return listOrdersByVisitId(visitId).stream()
                    .map(order -> OrderResponse.from(order))
                    .collect(Collectors.toList());
        }
        String u = f.toUpperCase();
        if (!"PRESCRIPTION".equals(u) && !"TEST".equals(u) && !"TREATMENT".equals(u)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        if ("PRESCRIPTION".equals(u)) {
            return listPrescriptionOrdersMerged(visitId);
        }
        return listOrdersByVisitId(visitId).stream()
                .filter(o -> o.getOrderType() != null && o.getOrderType().matchesApiOrderTypeFilter(u))
                .map(order -> OrderResponse.from(order))
                .collect(Collectors.toList());
    }

    private List<OrderResponse> listPrescriptionOrdersMerged(Long visitId) {
        List<AbstractMap.SimpleEntry<OrderResponse, Instant>> keyed = new ArrayList<>();
        for (Order o : listOrdersByVisitId(visitId)) {
            if (o.getOrderType() == null || !o.getOrderType().isPrescription()) {
                continue;
            }
            if (isCancelledForList(o.getOrderStatus())) {
                continue;
            }
            for (OrderItem item : o.getItems()) {
                keyed.add(new AbstractMap.SimpleEntry<>(singleItemOrderViewFromEntity(o, item), sortInstant(o, item)));
            }
        }
        for (SoapRx rx : soapRxRepo.findByVisitIdOrderByPrescriptionIdAsc(visitId)) {
            if (!orderRepo.existsByLegacyPrescriptionId(rx.getPrescriptionId())) {
                keyed.add(new AbstractMap.SimpleEntry<>(legacySoapOrderResponse(rx), soapRxInstant(rx)));
            }
        }
        keyed.sort(
                Comparator.comparing(
                                (AbstractMap.SimpleEntry<OrderResponse, Instant> entry) -> entry.getValue())
                        .reversed());
        return keyed.stream()
                .map((AbstractMap.SimpleEntry<OrderResponse, Instant> entry) -> entry.getKey())
                .collect(Collectors.toList());
    }

    private static OrderResponse singleItemOrderViewFromEntity(Order o, OrderItem item) {
        OrderItemResponse ir = OrderItemResponse.from(item);
        return new OrderResponse(
                o.getOrderId(),
                o.getVisitId(),
                o.getOrderType() != null ? o.getOrderType().name() : null,
                o.getOrderStatus(),
                o.getDoctorId(),
                o.getOrderDate(),
                o.getCreatedAt(),
                o.getUpdatedAt(),
                List.of(ir));
    }

    @Override
    @Transactional
    public OrderResponse createOrder(Long visitId, OrderCreateRequest request) {
        Order saved = persistNewOrder(visitId, request);
        return OrderResponse.from(saved);
    }

    @Override
    @Transactional
    public OrderItemResponse updateOrderItemLine(
            Long visitId, Long orderId, Long orderItemId, OrderItemCreateRequest body) {
        if (isLegacySoapKey(orderId, orderItemId)) {
            long legacyId = -orderId;
            chartService.updateSoapRx(
                    visitId,
                    legacyId,
                    body != null ? body.getItemName() : null,
                    body != null ? body.getDosage() : null,
                    body != null ? body.getDuration() : null);
            SoapRx rx = soapRxRepo
                    .findByPrescriptionIdAndVisitId(legacyId, visitId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));
            return legacySoapItemResponse(rx);
        }
        Order o = getOrder(orderId);
        if (!o.getVisitId().equals(visitId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        if (o.getOrderType() != null && o.getOrderType().isPrescription()) {
            return OrderItemResponse.from(updatePrescriptionItem(visitId, orderId, orderItemId, body));
        }
        return OrderItemResponse.from(updateOrderItem(orderItemId, body));
    }

    @Override
    @Transactional
    public void deleteOrderItemLine(Long visitId, Long orderId, Long orderItemId) {
        if (isLegacySoapKey(orderId, orderItemId)) {
            chartService.removeSoapRx(visitId, -orderId);
            return;
        }
        Order o = getOrder(orderId);
        if (!o.getVisitId().equals(visitId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        if (o.getOrderType() != null && o.getOrderType().isPrescription()) {
            deletePrescriptionItem(visitId, orderId, orderItemId);
        } else {
            deleteOrderItem(orderId, orderItemId);
        }
    }

    @Override
    public Order getOrder(Long orderId) {
        return orderRepo.findByIdWithItems(orderId).orElseThrow(() -> new OrderNotFoundException());
    }

    @Override
    public List<Order> listOrdersByVisitId(Long visitId) {
        if (!visitRepo.existsById(visitId)) {
            return List.of();
        }
        return orderRepo.findByVisitIdOrderByOrderDateDesc(visitId);
    }

    @Override
    @Transactional
    public Order updateOrderStatus(Long visitId, Long orderId, String orderStatus) {
        if (orderStatus == null || orderStatus.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        String next = orderStatus.trim().toUpperCase();
        if (!"CANCELLED".equals(next)) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_CLINICIAN_FORBIDDEN);
        }
        Order o = requireOrderForVisit(visitId, orderId);
        assertClinicianMayCancel(o);
        o.setOrderStatus("CANCELLED");
        return orderRepo.save(o);
    }

    @Override
    @Transactional
    public Order cancelOrder(Long visitId, Long orderId) {
        return updateOrderStatus(visitId, orderId, "CANCELLED");
    }

    @Override
    @Transactional
    public Order syncOrderStatusFromSupport(Long visitId, Long orderId, String orderStatus) {
        if (orderStatus == null || orderStatus.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        String next = orderStatus.trim().toUpperCase();
        if ("REQUEST".equals(next)) {
            next = "REQUESTED";
        }
        if (!SUPPORT_ALLOWED_ORDER_STATUS.contains(next)) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        Order o = requireOrderForVisit(visitId, orderId);
        o.setOrderStatus(next);
        return orderRepo.save(o);
    }

    @Override
    @Transactional
    public OrderItem createOrderItem(Long orderId, OrderItemCreateRequest request) {
        Order order = orderRepo.findById(orderId).orElseThrow(() -> new OrderNotFoundException());
        validateOrderItems(order.getOrderType(), List.of(request));
        OrderItem item = toOrderItem(order.getOrderType(), request);
        order.addItem(item);
        orderRepo.save(order);
        if (order.getOrderType() != null
                && order.getOrderType().isLabCommittedType()
                && item.getOrderItemId() != null) {
            eventPublisher.publishEvent(
                    new LabOrderCommittedEvent(order.getOrderType(), List.of(item.getOrderItemId()), order.getDoctorId()));
        }
        return item;
    }

    @Override
    public OrderItem getOrderItem(Long orderItemId) {
        return orderItemRepo
                .findById(orderItemId)
                .orElseThrow(() -> new IllegalArgumentException("OrderItem not found: " + orderItemId));
    }

    @Override
    public List<OrderItem> listOrderItemsByOrderId(Long orderId) {
        return orderItemRepo.findByOrder_OrderId(orderId);
    }

    @Override
    @Transactional
    public OrderItem updateOrderItem(Long orderItemId, OrderItemCreateRequest request) {
        OrderItem item = getOrderItem(orderItemId);
        if (request.getItemCode() != null) {
            item.setItemCode(request.getItemCode());
        }
        if (request.getItemName() != null) {
            item.setItemName(request.getItemName().trim());
        }
        if (request.getDosage() != null) {
            item.setItemDosage(trimToNull(request.getDosage()));
        }
        if (request.getDose() != null) {
            item.setDose(request.getDose());
        }
        if (request.getFrequency() != null) {
            item.setFrequency(trimToNull(request.getFrequency()));
        }
        if (request.getDuration() != null) {
            item.setDuration(trimToNull(request.getDuration()));
        }
        return orderItemRepo.save(item);
    }

    @Override
    @Transactional
    public void deleteOrderItem(Long orderId, Long orderItemId) {
        Order order = orderRepo.findByIdWithItems(orderId).orElseThrow(() -> new OrderNotFoundException());
        order.getItems().removeIf(i -> i.getOrderItemId().equals(orderItemId));
        if (order.getItems().isEmpty()) {
            orderRepo.delete(order);
        } else {
            orderRepo.save(order);
        }
    }

    @Override
    @Transactional
    public OrderItem updatePrescriptionItem(
            Long visitId, Long orderId, Long orderItemId, OrderItemCreateRequest request) {
        Order o = requireOrderForVisit(visitId, orderId);
        if (o.getOrderType() == null || !o.getOrderType().isPrescription()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        OrderItem item = getOrderItem(orderItemId);
        if (item.getOrder() == null || !item.getOrder().getOrderId().equals(orderId)) {
            throw new OrderNotFoundException();
        }
        return updateOrderItem(orderItemId, request);
    }

    @Override
    @Transactional
    public void deletePrescriptionItem(Long visitId, Long orderId, Long orderItemId) {
        Order o = requireOrderForVisit(visitId, orderId);
        if (o.getOrderType() == null || !o.getOrderType().isPrescription()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        OrderItem item = getOrderItem(orderItemId);
        if (item.getOrder() == null || !item.getOrder().getOrderId().equals(orderId)) {
            throw new OrderNotFoundException();
        }
        deleteOrderItem(orderId, orderItemId);
    }

    @Override
    @Transactional
    public OrderResult createOrderResult(Long orderItemId, String resultValue, String resultStatus) {
        if (!orderItemRepo.existsById(orderItemId)) {
            throw new IllegalArgumentException("OrderItem not found: " + orderItemId);
        }
        return orderResultRepo.save(OrderResult.create(orderItemId, resultValue, resultStatus));
    }

    @Override
    public OrderResult getOrderResult(Long resultId) {
        return orderResultRepo
                .findById(resultId)
                .orElseThrow(() -> new IllegalArgumentException("OrderResult not found: " + resultId));
    }

    @Override
    public List<OrderResult> listOrderResultsByOrderItemId(Long orderItemId) {
        return orderResultRepo.findByOrderItemIdOrderByResultDateDesc(orderItemId);
    }

    @Override
    @Transactional
    public OrderResult updateOrderResult(Long resultId, String resultValue, String resultStatus) {
        OrderResult r = getOrderResult(resultId);
        if (resultValue != null) {
            r.setResultValue(resultValue);
        }
        if (resultStatus != null) {
            r.setResultStatus(resultStatus);
        }
        return orderResultRepo.save(r);
    }

    private Order persistNewOrder(Long visitId, OrderCreateRequest request) {
        if (!visitRepo.existsById(visitId)) {
            throw new VisitNotFoundException();
        }
        OrderType orderType;
        try {
            orderType = OrderType.fromApi(request.getOrderType());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        if (orderType == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        List<OrderItemCreateRequest> items = request.getItems() != null ? request.getItems() : List.of();
        if (orderType.isPrescription()) {
            if (items.isEmpty()) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST);
            }
            validateOrderItems(orderType, items);
        } else if (!items.isEmpty()) {
            validateOrderItems(orderType, items);
        }
        Order o = Order.create(visitId, orderType, "REQUESTED", request.getDoctorId());
        for (OrderItemCreateRequest req : items) {
            o.addItem(toOrderItem(orderType, req));
        }
        Order saved = orderRepo.save(o);
        publishLabOrderCommittedIfNeeded(saved);
        return saved;
    }

    private Order requireOrderForVisit(Long visitId, Long orderId) {
        Order o = orderRepo.findByIdWithItems(orderId).orElseThrow(() -> new OrderNotFoundException());
        if (!o.getVisitId().equals(visitId)) {
            throw new VisitNotFoundException();
        }
        return o;
    }

    private static void assertClinicianMayCancel(Order o) {
        String s = normalizeOrderStatusForRule(o.getOrderStatus());
        if ("COMPLETED".equals(s) || "CANCELLED".equals(s)) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_NOT_CANCELLABLE);
        }
    }

    private static String normalizeOrderStatusForRule(String raw) {
        if (raw == null || raw.isBlank()) {
            return "REQUESTED";
        }
        String s = raw.trim().toUpperCase();
        return "REQUEST".equals(s) ? "REQUESTED" : s;
    }

    private void publishLabOrderCommittedIfNeeded(Order order) {
        if (order.getOrderType() == null
                || !order.getOrderType().isLabCommittedType()
                || order.getItems() == null
                || order.getItems().isEmpty()) {
            return;
        }
        List<Long> ids = order.getItems().stream()
                .map(item -> item.getOrderItemId())
                .filter(id -> id != null)
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            return;
        }
        eventPublisher.publishEvent(new LabOrderCommittedEvent(order.getOrderType(), ids, order.getDoctorId()));
    }

    private static void validateOrderItems(OrderType orderType, List<OrderItemCreateRequest> items) {
        if (orderType == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        if (items == null || items.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        if (orderType.isPrescription()) {
            for (OrderItemCreateRequest req : items) {
                if (req.getItemName() == null || req.getItemName().isBlank()) {
                    throw new BusinessException(ErrorCode.INVALID_REQUEST);
                }
            }
        } else {
            for (OrderItemCreateRequest req : items) {
                if (req.getItemCode() == null || req.getItemCode().isBlank()) {
                    throw new BusinessException(ErrorCode.INVALID_REQUEST);
                }
            }
        }
    }

    private static OrderItem toOrderItem(OrderType orderType, OrderItemCreateRequest req) {
        if (orderType == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        if (orderType.isPrescription()) {
            return OrderItem.createPrescriptionLine(
                    req.getItemName().trim(),
                    trimToNull(req.getDosage()),
                    trimToNull(req.getFrequency()),
                    trimToNull(req.getDuration()));
        }
        OrderItem item =
                OrderItem.create(
                        req.getItemCode().trim(),
                        req.getDose(),
                        trimToNull(req.getFrequency()),
                        trimToNull(req.getDuration()));
        if (req.getItemName() != null && !req.getItemName().isBlank()) {
            item.setItemName(req.getItemName().trim());
        }
        return item;
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static boolean isLegacySoapKey(long orderId, long orderItemId) {
        return orderId < 0 && orderItemId == orderId;
    }

    private static boolean isCancelledForList(String orderStatus) {
        if (orderStatus == null || orderStatus.isBlank()) {
            return false;
        }
        return "CANCELLED".equalsIgnoreCase(orderStatus.trim());
    }

    private static Instant sortInstant(Order o, OrderItem item) {
        LocalDateTime t = item.getCreatedAt() != null ? item.getCreatedAt() : o.getOrderDate();
        if (t == null) {
            return Instant.EPOCH;
        }
        return t.atZone(ZoneId.systemDefault()).toInstant();
    }

    private static Instant soapRxInstant(SoapRx rx) {
        LocalDateTime t = rx.getCreatedAt();
        if (t == null) {
            return Instant.EPOCH;
        }
        return t.atZone(ZoneId.systemDefault()).toInstant();
    }

    private static OrderResponse legacySoapOrderResponse(SoapRx rx) {
        long sid = -rx.getPrescriptionId();
        OrderItemResponse item = new OrderItemResponse(
                sid,
                sid,
                null,
                rx.getMedicationName(),
                rx.getDosage(),
                null,
                null,
                rx.getDays(),
                rx.getCreatedAt());
        return new OrderResponse(
                sid,
                rx.getVisitId(),
                OrderType.PRESCRIPTION.name(),
                "REQUESTED",
                null,
                rx.getCreatedAt(),
                rx.getCreatedAt(),
                rx.getUpdatedAt(),
                List.of(item));
    }

    private static OrderItemResponse legacySoapItemResponse(SoapRx rx) {
        long sid = -rx.getPrescriptionId();
        return new OrderItemResponse(
                sid,
                sid,
                null,
                rx.getMedicationName(),
                rx.getDosage(),
                null,
                null,
                rx.getDays(),
                rx.getCreatedAt());
    }
}
