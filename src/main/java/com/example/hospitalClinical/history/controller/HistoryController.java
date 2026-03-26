package com.example.hospitalClinical.history.controller;

import com.example.hospitalClinical.common.response.ApiResponse;
import com.example.hospitalClinical.history.dto.HistoryResponse;
import com.example.hospitalClinical.history.dto.HistoryUpdateRequest;
import com.example.hospitalClinical.history.service.HistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = {"http://localhost:3001", "http://127.0.0.1:3001", "http://localhost:5173", "http://192.168.1.64:3001"})
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/visits/{visitId}/past-history")
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<HistoryResponse>>> list(@PathVariable("visitId") Long visitId) {
        log.info("[GET] /api/visits/{}/past-history - 과거력 목록 조회", visitId);
        List<HistoryResponse> list = historyService.listByVisitId(visitId).stream()
                .map(HistoryResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(true, "과거력 목록 조회 성공", list));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<HistoryResponse>> create(
            @PathVariable("visitId") Long visitId,
            @RequestBody HistoryUpdateRequest request) {
        log.info("[POST] /api/visits/{}/past-history - 과거력 등록", visitId);
        HistoryResponse result = HistoryResponse.from(historyService.saveByVisitId(visitId, request));
        return ResponseEntity.status(201).body(new ApiResponse<>(true, "과거력 등록 성공", result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HistoryResponse>> update(
            @PathVariable("visitId") Long visitId,
            @PathVariable("id") Long id,
            @RequestBody HistoryUpdateRequest request) {
        log.info("[PUT] /api/visits/{}/past-history/{} - 과거력 수정", visitId, id);
        HistoryResponse result = HistoryResponse.from(historyService.update(id, request));
        return ResponseEntity.ok(new ApiResponse<>(true, "과거력 수정 성공", result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable("visitId") Long visitId,
            @PathVariable("id") Long id) {
        log.info("[DELETE] /api/visits/{}/past-history/{} - 과거력 삭제", visitId, id);
        historyService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "과거력 삭제 성공", null));
    }
}
