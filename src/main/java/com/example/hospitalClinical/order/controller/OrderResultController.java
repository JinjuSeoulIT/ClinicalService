package com.example.hospitalClinical.order.controller;

import com.example.hospitalClinical.common.response.ApiResponse;
import com.example.hospitalClinical.order.dto.OrderResultResponse;
import com.example.hospitalClinical.order.service.OrderService;
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
@RequestMapping("/api/visits/{visitId}/orders/{orderId}/items/{orderItemId}/results")
public class OrderResultController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResultResponse>> create(
            @PathVariable("visitId") Long visitId,
            @PathVariable("orderId") Long orderId,
            @PathVariable("orderItemId") Long orderItemId,
            @RequestBody Map<String, String> body) {
        log.info("[POST] /api/.../items/{}/results - 결과 등록", orderItemId);
        String resultValue = body != null ? body.get("resultValue") : null;
        String resultStatus = body != null ? body.get("resultStatus") : null;
        OrderResultResponse result = OrderResultResponse.from(orderService.createOrderResult(orderItemId, resultValue, resultStatus));
        return ResponseEntity.status(201).body(new ApiResponse<>(true, "결과 등록 성공", result));
    }

    @GetMapping("/{resultId}")
    public ResponseEntity<ApiResponse<OrderResultResponse>> get(
            @PathVariable("visitId") Long visitId,
            @PathVariable("orderId") Long orderId,
            @PathVariable("orderItemId") Long orderItemId,
            @PathVariable("resultId") Long resultId) {
        log.info("[GET] /api/.../results/{} - 결과 조회", resultId);
        OrderResultResponse result = OrderResultResponse.from(orderService.getOrderResult(resultId));
        return ResponseEntity.ok(new ApiResponse<>(true, "결과 조회 성공", result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResultResponse>>> list(
            @PathVariable("visitId") Long visitId,
            @PathVariable("orderId") Long orderId,
            @PathVariable("orderItemId") Long orderItemId) {
        log.info("[GET] /api/.../items/{}/results - 결과 목록 조회", orderItemId);
        List<OrderResultResponse> list = orderService.listOrderResultsByOrderItemId(orderItemId).stream()
                .map(OrderResultResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(true, "결과 목록 조회 성공", list));
    }

    @PatchMapping("/{resultId}")
    public ResponseEntity<ApiResponse<OrderResultResponse>> update(
            @PathVariable("visitId") Long visitId,
            @PathVariable("orderId") Long orderId,
            @PathVariable("orderItemId") Long orderItemId,
            @PathVariable("resultId") Long resultId,
            @RequestBody Map<String, String> body) {
        log.info("[PATCH] /api/.../results/{} - 결과 수정", resultId);
        String resultValue = body != null ? body.get("resultValue") : null;
        String resultStatus = body != null ? body.get("resultStatus") : null;
        OrderResultResponse result = OrderResultResponse.from(orderService.updateOrderResult(resultId, resultValue, resultStatus));
        return ResponseEntity.ok(new ApiResponse<>(true, "결과 수정 성공", result));
    }
}
