package com.example.hospitalClinical.order.service;

import com.example.hospitalClinical.order.dto.OrderCreateRequest;
import com.example.hospitalClinical.order.dto.OrderItemCreateRequest;
import com.example.hospitalClinical.order.entity.Order;
import com.example.hospitalClinical.order.entity.OrderItem;
import com.example.hospitalClinical.order.entity.OrderResult;

import java.util.List;

public interface OrderService {

    Order createOrder(Long visitId, OrderCreateRequest request);
    Order getOrder(Long orderId);
    List<Order> listOrdersByVisitId(Long visitId);
    Order updateOrderStatus(Long visitId, Long orderId, String orderStatus);
    Order cancelOrder(Long visitId, Long orderId);
    Order syncOrderStatusFromSupport(Long visitId, Long orderId, String orderStatus);

    OrderItem createOrderItem(Long orderId, OrderItemCreateRequest request);
    OrderItem getOrderItem(Long orderItemId);
    List<OrderItem> listOrderItemsByOrderId(Long orderId);
    OrderItem updateOrderItem(Long orderItemId, OrderItemCreateRequest request);
    void deleteOrderItem(Long orderId, Long orderItemId);

    OrderResult createOrderResult(Long orderItemId, String resultValue, String resultStatus);
    OrderResult getOrderResult(Long resultId);
    List<OrderResult> listOrderResultsByOrderItemId(Long orderItemId);
    OrderResult updateOrderResult(Long resultId, String resultValue, String resultStatus);
}
