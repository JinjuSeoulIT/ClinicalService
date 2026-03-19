package com.example.hospitalClinical.order.controller;

import com.example.hospitalClinical.common.response.ApiResponse;
import com.example.hospitalClinical.order.dto.OrderCreateRequest;
import com.example.hospitalClinical.order.dto.OrderResponse;
import com.example.hospitalClinical.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = {"http://localhost:3001", "http://127.0.0.1:3001", "http://localhost:5173"})
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/visits/{visitId}/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> create(
            @PathVariable("visitId") Long visitId,
            @RequestBody @Valid OrderCreateRequest request) {
        log.info("[POST] /api/visits/{}/orders - 오더 등록", visitId);
        OrderResponse result = OrderResponse.from(orderService.createOrder(visitId, request));
        return ResponseEntity.status(201).body(new ApiResponse<>(true, "오더 등록 성공", result));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> get(
            @PathVariable("visitId") Long visitId,
            @PathVariable("orderId") Long orderId) {
        log.info("[GET] /api/visits/{}/orders/{} - 오더 조회", visitId, orderId);
        OrderResponse result = OrderResponse.from(orderService.getOrder(orderId));
        return ResponseEntity.ok(new ApiResponse<>(true, "오더 조회 성공", result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> list(@PathVariable("visitId") Long visitId) {
        log.info("[GET] /api/visits/{}/orders - 오더 목록 조회", visitId);
        List<OrderResponse> list = orderService.listOrdersByVisitId(visitId).stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(true, "오더 목록 조회 성공", list));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable("visitId") Long visitId,
            @PathVariable("orderId") Long orderId,
            @RequestBody Map<String, String> body) {
        log.info("[PATCH] /api/visits/{}/orders/{}/status - 오더 상태 변경", visitId, orderId);
        String status = body != null ? body.get("orderStatus") : null;
        OrderResponse result = OrderResponse.from(orderService.updateOrderStatus(visitId, orderId, status));
        return ResponseEntity.ok(new ApiResponse<>(true, "오더 상태 변경 성공", result));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancel(
            @PathVariable("visitId") Long visitId,
            @PathVariable("orderId") Long orderId) {
        log.info("[POST] /api/visits/{}/orders/{}/cancel - 오더 취소", visitId, orderId);
        OrderResponse result = OrderResponse.from(orderService.cancelOrder(visitId, orderId));
        return ResponseEntity.ok(new ApiResponse<>(true, "오더 취소 성공", result));
    }
}
