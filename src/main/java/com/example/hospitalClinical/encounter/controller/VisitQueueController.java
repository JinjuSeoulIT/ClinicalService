package com.example.hospitalClinical.encounter.controller;

import com.example.hospitalClinical.common.response.ApiResponse;
import com.example.hospitalClinical.encounter.dto.VisitQueueResponse;
import com.example.hospitalClinical.encounter.service.EncounterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = {"http://localhost:3001", "http://127.0.0.1:3001", "http://localhost:5173", "http://192.168.1.64:3001"})
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/visit-queue")
public class VisitQueueController {

    private final EncounterService encounterService;

    @PostMapping
    public ResponseEntity<ApiResponse<VisitQueueResponse>> create(@RequestBody Map<String, Object> body) {
        log.info("[POST] /api/visit-queue - 대기열 등록");
        Long visitId = body != null && body.get("visitId") != null ? Long.valueOf(body.get("visitId").toString()) : null;
        Integer queueOrder = body != null && body.get("queueOrder") != null ? (Integer) body.get("queueOrder") : null;
        Long roomId = body != null && body.get("roomId") != null ? Long.valueOf(body.get("roomId").toString()) : null;
        VisitQueueResponse result = VisitQueueResponse.from(encounterService.createQueue(visitId, queueOrder, roomId));
        return ResponseEntity.status(201).body(new ApiResponse<>(true, "대기열 등록 성공", result));
    }

    @GetMapping("/{queueId}")
    public ResponseEntity<ApiResponse<VisitQueueResponse>> get(@PathVariable("queueId") Long queueId) {
        log.info("[GET] /api/visit-queue/{} - 대기열 조회", queueId);
        VisitQueueResponse result = VisitQueueResponse.from(encounterService.getQueue(queueId));
        return ResponseEntity.ok(new ApiResponse<>(true, "대기열 조회 성공", result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VisitQueueResponse>>> list(
            @RequestParam(value = "visitId", required = false) Long visitId) {
        log.info("[GET] /api/visit-queue - 대기열 목록 조회");
        List<VisitQueueResponse> list;
        if (visitId != null) {
            list = encounterService.listQueueByVisitId(visitId).stream().map(VisitQueueResponse::from).collect(Collectors.toList());
        } else {
            list = encounterService.listAllQueue().stream().map(VisitQueueResponse::from).collect(Collectors.toList());
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "대기열 목록 조회 성공", list));
    }
}
