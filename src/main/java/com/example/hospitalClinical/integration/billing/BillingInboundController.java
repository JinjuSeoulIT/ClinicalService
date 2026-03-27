package com.example.hospitalClinical.integration.billing;

import com.example.hospitalClinical.common.response.ApiResponse;
import com.example.hospitalClinical.integration.billing.dto.BillingInboundAck;
import com.example.hospitalClinical.integration.billing.dto.BillingInboundNotificationRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://127.0.0.1:3000", "http://127.0.0.1:3001", "http://localhost:5173", "http://192.168.1.64:3001"})
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/integration/billing")
public class BillingInboundController {

    private final ObjectMapper objectMapper;

    @PostMapping("/notifications")
    public ResponseEntity<ApiResponse<BillingInboundAck>> receive(@Valid @RequestBody BillingInboundNotificationRequest body) {
        try {
            log.info("[수납→진료] body={}", objectMapper.writeValueAsString(body));
        } catch (JsonProcessingException e) {
            log.info("[수납→진료] eventType={} receptionId={} visitId={} note={}",
                    body.getEventType(), body.getReceptionId(), body.getVisitId(), body.getNote());
        }
        String at = Instant.now().toString();
        BillingInboundAck ack = new BillingInboundAck(
                body.getEventType(),
                body.getReceptionId(),
                body.getVisitId(),
                body.getNote(),
                at
        );
        return ResponseEntity.ok(ApiResponse.ok("수납 연동 알림 수신", ack));
    }
}
