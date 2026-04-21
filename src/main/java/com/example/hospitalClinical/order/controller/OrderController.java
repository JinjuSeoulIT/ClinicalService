package com.example.hospitalClinical.order.controller;

import com.example.hospitalClinical.common.exception.BusinessException;
import com.example.hospitalClinical.common.response.ApiResponse;
import com.example.hospitalClinical.order.dto.OrderCreateRequest;
import com.example.hospitalClinical.order.dto.OrderResponse;
import com.example.hospitalClinical.order.integration.clinicalsupport.inbound.SupportTestOrderStatusRefresh;
import com.example.hospitalClinical.order.service.OrderVisitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = {
        "http://localhost:3000",
        "http://localhost:3001",
        "http://127.0.0.1:3000",
        "http://127.0.0.1:3001",
        "http://localhost:5173",
        "http://192.168.1.64:3001",
        "http://192.168.1.70:3001"
})
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/visits/{visitId}/orders")
public class OrderController {

    private final OrderVisitService orderVisitService;
    private final SupportTestOrderStatusRefresh supportTestOrderStatusRefresh;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> create(
            @PathVariable("visitId") Long visitId,
            @RequestBody @Valid OrderCreateRequest request) {
        OrderResponse result = orderVisitService.createOrder(visitId, request);
        log.info(
                "[POST] /api/visits/{}/orders - 오더 등록 완료 orderId={} itemCount={}",
                visitId,
                result.getOrderId(),
                result.getItems() != null ? result.getItems().size() : 0);
        return ResponseEntity.status(201).body(new ApiResponse<>(true, "오더 등록 성공", result));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> get(
            @PathVariable("visitId") Long visitId,
            @PathVariable("orderId") Long orderId) {
        log.info("[GET] /api/visits/{}/orders/{} - 오더 조회", visitId, orderId);
        OrderResponse result = OrderResponse.from(orderVisitService.getOrder(orderId));
        return ResponseEntity.ok(new ApiResponse<>(true, "오더 조회 성공", result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> list(
            @PathVariable("visitId") Long visitId,
            @RequestParam(value = "orderType", required = false) String orderType,
            @RequestParam(value = "syncFromSupport", defaultValue = "false") boolean syncFromSupport) {
        log.info("[GET] /api/visits/{}/orders orderType={} syncFromSupport={}", visitId, orderType, syncFromSupport);
        try {
            if (syncFromSupport) {
                supportTestOrderStatusRefresh.refreshForVisit(visitId, orderType);
            }
            List<OrderResponse> list = orderVisitService.listOrders(visitId, orderType);
            return ResponseEntity.ok(new ApiResponse<>(true, "오더 목록 조회 성공", list));
        } catch (BusinessException ex) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, ex.getMessage(), null));
        }
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable("visitId") Long visitId,
            @PathVariable("orderId") Long orderId,
            @RequestBody Map<String, String> body) {
        log.info("[PATCH] /api/visits/{}/orders/{}/status - 오더 요청 취소(PATCH)", visitId, orderId);
        String status = body != null ? body.get("orderStatus") : null;
        OrderResponse result = OrderResponse.from(orderVisitService.updateOrderStatus(visitId, orderId, status));
        return ResponseEntity.ok(new ApiResponse<>(true, "오더 취소 성공", result));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancel(
            @PathVariable("visitId") Long visitId,
            @PathVariable("orderId") Long orderId) {
        log.info("[POST] /api/visits/{}/orders/{}/cancel - 오더 취소", visitId, orderId);
        OrderResponse result = OrderResponse.from(orderVisitService.cancelOrder(visitId, orderId));
        return ResponseEntity.ok(new ApiResponse<>(true, "오더 취소 성공", result));
    }
}
