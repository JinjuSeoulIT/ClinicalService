package com.example.hospitalClinical.order.controller;

import com.example.hospitalClinical.common.response.ApiResponse;
import com.example.hospitalClinical.order.dto.OrderItemCreateRequest;
import com.example.hospitalClinical.order.dto.OrderItemResponse;
import com.example.hospitalClinical.order.service.OrderVisitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = {
        "http://localhost:3000",
        "http://localhost:3001",
        "http://127.0.0.1:3000",
        "http://127.0.0.1:3001",
        "http://localhost:5173",
        "http://192.168.1.*",
        "http://192.168.1.64:3001",
        "http://192.168.1.70:3001"
})
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/visits/{visitId}/orders/{orderId}/items")
public class OrderItemController {

    private final OrderVisitService orderVisitService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderItemResponse>> create(
            @PathVariable("visitId") Long visitId,
            @PathVariable("orderId") Long orderId,
            @RequestBody @Valid OrderItemCreateRequest request) {
        log.info("[POST] /api/visits/{}/orders/{}/items - 오더 항목 등록", visitId, orderId);
        OrderItemResponse result = OrderItemResponse.from(orderVisitService.createOrderItem(orderId, request));
        return ResponseEntity.status(201).body(new ApiResponse<>(true, "오더 항목 등록 성공", result));
    }

    @GetMapping("/{orderItemId}")
    public ResponseEntity<ApiResponse<OrderItemResponse>> get(
            @PathVariable("visitId") Long visitId,
            @PathVariable("orderId") Long orderId,
            @PathVariable("orderItemId") Long orderItemId) {
        log.info("[GET] /api/visits/{}/orders/{}/items/{} - 오더 항목 조회", visitId, orderId, orderItemId);
        OrderItemResponse result = OrderItemResponse.from(orderVisitService.getOrderItem(orderItemId));
        return ResponseEntity.ok(new ApiResponse<>(true, "오더 항목 조회 성공", result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderItemResponse>>> list(
            @PathVariable("visitId") Long visitId,
            @PathVariable("orderId") Long orderId) {
        log.info("[GET] /api/visits/{}/orders/{}/items - 오더 항목 목록 조회", visitId, orderId);
        List<OrderItemResponse> list = orderVisitService.listOrderItemsByOrderId(orderId).stream()
                .map(OrderItemResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(true, "오더 항목 목록 조회 성공", list));
    }

    @PatchMapping("/{orderItemId}")
    public ResponseEntity<ApiResponse<OrderItemResponse>> update(
            @PathVariable("visitId") Long visitId,
            @PathVariable("orderId") Long orderId,
            @PathVariable("orderItemId") Long orderItemId,
            @RequestBody @Valid OrderItemCreateRequest request) {
        log.info("[PATCH] /api/visits/{}/orders/{}/items/{} - 오더 항목 수정", visitId, orderId, orderItemId);
        OrderItemResponse result = orderVisitService.updateOrderItemLine(visitId, orderId, orderItemId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "오더 항목 수정 성공", result));
    }

    @DeleteMapping("/{orderItemId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable("visitId") Long visitId,
            @PathVariable("orderId") Long orderId,
            @PathVariable("orderItemId") Long orderItemId) {
        log.info("[DELETE] /api/visits/{}/orders/{}/items/{} - 오더 항목 삭제", visitId, orderId, orderItemId);
        orderVisitService.deleteOrderItemLine(visitId, orderId, orderItemId);
        return ResponseEntity.noContent().build();
    }
}
