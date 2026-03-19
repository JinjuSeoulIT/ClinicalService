package com.example.hospitalClinical.order.service;

import com.example.hospitalClinical.encounter.exception.VisitNotFoundException;
import com.example.hospitalClinical.encounter.repository.VisitRepo;
import com.example.hospitalClinical.order.dto.OrderCreateRequest;
import com.example.hospitalClinical.order.dto.OrderItemCreateRequest;
import com.example.hospitalClinical.order.entity.Order;
import com.example.hospitalClinical.order.entity.OrderItem;
import com.example.hospitalClinical.order.entity.OrderResult;
import com.example.hospitalClinical.order.exception.OrderNotFoundException;
import com.example.hospitalClinical.order.repository.OrderItemRepo;
import com.example.hospitalClinical.order.repository.OrderRepo;
import com.example.hospitalClinical.order.repository.OrderResultRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepo orderRepo;
    private final OrderItemRepo orderItemRepo;
    private final OrderResultRepo orderResultRepo;
    private final VisitRepo visitRepo;

    @Override
    @Transactional
    public Order createOrder(Long visitId, OrderCreateRequest request) {
        if (!visitRepo.existsById(visitId)) throw new VisitNotFoundException();
        Order o = Order.create(visitId, request.getOrderType(), "REQUESTED", request.getDoctorId());
        List<OrderItemCreateRequest> items = request.getItems();
        if (items != null) {
            for (OrderItemCreateRequest req : items) {
                o.addItem(OrderItem.create(req.getItemCode(), req.getDose(), req.getFrequency(), req.getDuration()));
            }
        }
        return orderRepo.save(o);
    }

    @Override
    public Order getOrder(Long orderId) {
        return orderRepo.findByIdWithItems(orderId).orElseThrow(OrderNotFoundException::new);
    }

    @Override
    public List<Order> listOrdersByVisitId(Long visitId) {
        if (!visitRepo.existsById(visitId)) return List.of();
        return orderRepo.findByVisitIdOrderByOrderDateDesc(visitId);
    }

    @Override
    @Transactional
    public Order updateOrderStatus(Long visitId, Long orderId, String orderStatus) {
        Order o = orderRepo.findByIdWithItems(orderId).orElseThrow(OrderNotFoundException::new);
        if (!o.getVisitId().equals(visitId)) throw new VisitNotFoundException();
        o.setOrderStatus(orderStatus);
        return orderRepo.save(o);
    }

    @Override
    @Transactional
    public Order cancelOrder(Long visitId, Long orderId) {
        return updateOrderStatus(visitId, orderId, "CANCELLED");
    }

    @Override
    @Transactional
    public OrderItem createOrderItem(Long orderId, OrderItemCreateRequest request) {
        Order order = orderRepo.findById(orderId).orElseThrow(OrderNotFoundException::new);
        OrderItem item = OrderItem.create(request.getItemCode(), request.getDose(), request.getFrequency(), request.getDuration());
        order.addItem(item);
        orderRepo.save(order);
        return item;
    }

    @Override
    public OrderItem getOrderItem(Long orderItemId) {
        return orderItemRepo.findById(orderItemId).orElseThrow(() -> new IllegalArgumentException("OrderItem not found: " + orderItemId));
    }

    @Override
    public List<OrderItem> listOrderItemsByOrderId(Long orderId) {
        return orderItemRepo.findByOrder_OrderId(orderId);
    }

    @Override
    @Transactional
    public OrderItem updateOrderItem(Long orderItemId, OrderItemCreateRequest request) {
        OrderItem item = getOrderItem(orderItemId);
        if (request.getItemCode() != null) item.setItemCode(request.getItemCode());
        if (request.getDose() != null) item.setDose(request.getDose());
        if (request.getFrequency() != null) item.setFrequency(request.getFrequency());
        if (request.getDuration() != null) item.setDuration(request.getDuration());
        return orderItemRepo.save(item);
    }

    @Override
    @Transactional
    public void deleteOrderItem(Long orderId, Long orderItemId) {
        Order order = orderRepo.findByIdWithItems(orderId).orElseThrow(OrderNotFoundException::new);
        order.getItems().removeIf(i -> i.getOrderItemId().equals(orderItemId));
        orderRepo.save(order);
    }

    @Override
    @Transactional
    public OrderResult createOrderResult(Long orderItemId, String resultValue, String resultStatus) {
        if (!orderItemRepo.existsById(orderItemId)) throw new IllegalArgumentException("OrderItem not found: " + orderItemId);
        return orderResultRepo.save(OrderResult.create(orderItemId, resultValue, resultStatus));
    }

    @Override
    public OrderResult getOrderResult(Long resultId) {
        return orderResultRepo.findById(resultId).orElseThrow(() -> new IllegalArgumentException("OrderResult not found: " + resultId));
    }

    @Override
    public List<OrderResult> listOrderResultsByOrderItemId(Long orderItemId) {
        return orderResultRepo.findByOrderItemIdOrderByResultDateDesc(orderItemId);
    }

    @Override
    @Transactional
    public OrderResult updateOrderResult(Long resultId, String resultValue, String resultStatus) {
        OrderResult r = getOrderResult(resultId);
        if (resultValue != null) r.setResultValue(resultValue);
        if (resultStatus != null) r.setResultStatus(resultStatus);
        return orderResultRepo.save(r);
    }
}
