package com.example.hospitalClinical.order.controller;

import com.example.hospitalClinical.common.response.ApiResponse;
import com.example.hospitalClinical.order.dto.OrderResponse;
import com.example.hospitalClinical.order.entity.Order;
import com.example.hospitalClinical.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = {"http://localhost:3001", "http://127.0.0.1:3001", "http://localhost:5173", "http://192.168.1.64:3001"})
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/clinical-support/visits/{visitId}/orders")
public class ClinicalSupportOrderController {

    private final OrderService orderService;

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> syncStatus(
            @PathVariable("visitId") Long visitId,
            @PathVariable("orderId") Long orderId,
            @RequestBody Map<String, String> body) {
        String status = body != null ? body.get("orderStatus") : null;
        log.info(
                "[PATCH] /api/clinical-support/visits/{}/orders/{}/status - 진료지원 오더 상태 동기화 status={}",
                visitId,
                orderId,
                status);
        Order saved = orderService.syncOrderStatusFromSupport(visitId, orderId, status);
        return ResponseEntity.ok(new ApiResponse<>(true, "오더 상태 동기화 성공", OrderResponse.from(saved)));
    }
}
