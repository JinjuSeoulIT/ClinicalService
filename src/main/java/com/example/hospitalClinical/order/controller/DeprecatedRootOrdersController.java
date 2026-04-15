package com.example.hospitalClinical.order.controller;

import com.example.hospitalClinical.common.exception.BusinessException;
import com.example.hospitalClinical.common.response.ApiResponse;
import com.example.hospitalClinical.order.dto.OrderCreateRequest;
import com.example.hospitalClinical.order.dto.OrderItemCreateRequest;
import com.example.hospitalClinical.order.dto.OrderItemResponse;
import com.example.hospitalClinical.order.dto.OrderResponse;
import com.example.hospitalClinical.order.dto.VisitOrderCreateRequest;
import com.example.hospitalClinical.order.entity.OrderType;
import com.example.hospitalClinical.order.service.OrderVisitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/orders")
public class DeprecatedRootOrdersController {

    private final OrderVisitService orderVisitService;

    @CrossOrigin(origins = {
            "http://localhost:3000",
            "http://localhost:3001",
            "http://127.0.0.1:3000",
            "http://127.0.0.1:3001",
            "http://localhost:5173",
            "http://192.168.1.64:3001",
            "http://192.168.1.70:3001"
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> list(
            @RequestParam("visitId") Long visitId,
            @RequestParam(value = "orderType", required = false, defaultValue = "PRESCRIPTION") String orderTypeParam) {
        log.warn("[DEPRECATED] GET /api/orders — use GET /api/visits/{visitId}/orders?orderType=...");
        try {
            List<OrderResponse> list = orderVisitService.listOrders(visitId, orderTypeParam);
            return ResponseEntity.ok()
                    .header("Deprecation", "true")
                    .body(new ApiResponse<>(true, "처방(오더) 목록 조회 성공 (deprecated API)", list));
        } catch (BusinessException ex) {
            return ResponseEntity.badRequest()
                    .header("Deprecation", "true")
                    .body(new ApiResponse<>(false, ex.getMessage(), null));
        }
    }

    @CrossOrigin(origins = {
            "http://localhost:3000",
            "http://localhost:3001",
            "http://127.0.0.1:3000",
            "http://127.0.0.1:3001",
            "http://localhost:5173",
            "http://192.168.1.64:3001",
            "http://192.168.1.70:3001"
    })
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> create(@RequestBody @Valid VisitOrderCreateRequest body) {
        log.warn("[DEPRECATED] POST /api/orders — use POST /api/visits/{visitId}/orders");
        OrderCreateRequest req = new OrderCreateRequest();
        BeanUtils.copyProperties(body, req);
        if (req.getOrderType() == null || req.getOrderType().isBlank()) {
            req.setOrderType(OrderType.PRESCRIPTION.name());
        }
        OrderResponse result = orderVisitService.createOrder(body.getVisitId(), req);
        return ResponseEntity.status(201)
                .header("Deprecation", "true")
                .body(new ApiResponse<>(true, "처방(오더) 등록 성공 (deprecated API)", result));
    }

    @CrossOrigin(origins = {
            "http://localhost:3000",
            "http://localhost:3001",
            "http://127.0.0.1:3000",
            "http://127.0.0.1:3001",
            "http://localhost:5173",
            "http://192.168.1.64:3001",
            "http://192.168.1.70:3001"
    })
    @PatchMapping("/{orderId}/items/{orderItemId}")
    public ResponseEntity<ApiResponse<OrderItemResponse>> updateItem(
            @RequestParam("visitId") Long visitId,
            @PathVariable("orderId") Long orderId,
            @PathVariable("orderItemId") Long orderItemId,
            @RequestBody OrderItemCreateRequest body) {
        log.warn("[DEPRECATED] PATCH /api/orders/.../items/... — use PATCH /api/visits/{visitId}/orders/...");
        OrderItemResponse updated = orderVisitService.updateOrderItemLine(visitId, orderId, orderItemId, body);
        return ResponseEntity.ok()
                .header("Deprecation", "true")
                .body(new ApiResponse<>(true, "처방 항목 수정 성공 (deprecated API)", updated));
    }

    @CrossOrigin(origins = {
            "http://localhost:3000",
            "http://localhost:3001",
            "http://127.0.0.1:3000",
            "http://127.0.0.1:3001",
            "http://localhost:5173",
            "http://192.168.1.64:3001",
            "http://192.168.1.70:3001"
    })
    @DeleteMapping("/{orderId}/items/{orderItemId}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(
            @RequestParam("visitId") Long visitId,
            @PathVariable("orderId") Long orderId,
            @PathVariable("orderItemId") Long orderItemId) {
        log.warn("[DEPRECATED] DELETE /api/orders/.../items/... — use DELETE /api/visits/{visitId}/orders/...");
        orderVisitService.deleteOrderItemLine(visitId, orderId, orderItemId);
        return ResponseEntity.ok()
                .header("Deprecation", "true")
                .body(new ApiResponse<>(true, "처방 항목 삭제 성공 (deprecated API)", null));
    }
}
