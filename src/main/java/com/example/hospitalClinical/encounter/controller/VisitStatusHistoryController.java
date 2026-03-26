package com.example.hospitalClinical.encounter.controller;

import com.example.hospitalClinical.common.response.ApiResponse;
import com.example.hospitalClinical.encounter.dto.VisitStatusHistoryResponse;
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
@RequestMapping("/api/visits/{visitId}/status-history")
public class VisitStatusHistoryController {

    private final EncounterService encounterService;

    @PostMapping
    public ResponseEntity<ApiResponse<VisitStatusHistoryResponse>> create(
            @PathVariable("visitId") Long visitId,
            @RequestBody Map<String, String> body) {
        log.info("[POST] /api/visits/{}/status-history - 상태 이력 등록", visitId);
        String status = body != null ? body.get("status") : null;
        VisitStatusHistoryResponse result = VisitStatusHistoryResponse.from(encounterService.createStatusHistory(visitId, status));
        return ResponseEntity.status(201).body(new ApiResponse<>(true, "상태 이력 등록 성공", result));
    }

    @GetMapping("/{historyId}")
    public ResponseEntity<ApiResponse<VisitStatusHistoryResponse>> get(
            @PathVariable("visitId") Long visitId,
            @PathVariable("historyId") Long historyId) {
        log.info("[GET] /api/visits/{}/status-history/{} - 상태 이력 조회", visitId, historyId);
        VisitStatusHistoryResponse result = VisitStatusHistoryResponse.from(encounterService.getStatusHistory(historyId));
        return ResponseEntity.ok(new ApiResponse<>(true, "상태 이력 조회 성공", result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VisitStatusHistoryResponse>>> list(@PathVariable("visitId") Long visitId) {
        log.info("[GET] /api/visits/{}/status-history - 상태 이력 목록 조회", visitId);
        List<VisitStatusHistoryResponse> list = encounterService.listStatusHistoryByVisitId(visitId).stream()
                .map(VisitStatusHistoryResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(true, "상태 이력 목록 조회 성공", list));
    }
}
