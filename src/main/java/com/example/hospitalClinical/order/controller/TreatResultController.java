package com.example.hospitalClinical.order.controller;

import com.example.hospitalClinical.common.response.ApiResponse;
import com.example.hospitalClinical.order.dto.TreatmentResultCreateRequest;
import com.example.hospitalClinical.order.dto.TreatmentResultResponse;
import com.example.hospitalClinical.order.service.OrderVisitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
@RequestMapping("/api/visits/{visitId}/treatment-results")
public class TreatResultController {

    private final OrderVisitService orderVisitService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TreatmentResultResponse>>> list(
            @PathVariable("visitId") Long visitId) {
        log.info("[GET] /api/visits/{}/treatment-results", visitId);
        List<TreatmentResultResponse> list = orderVisitService.listTreatmentResultsByVisit(visitId);
        return ResponseEntity.ok(new ApiResponse<>(true, "처치결과 목록 조회 성공", list));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TreatmentResultResponse>> create(
            @PathVariable("visitId") Long visitId,
            @RequestBody @Valid TreatmentResultCreateRequest request) {
        TreatmentResultResponse result = orderVisitService.createTreatmentResult(visitId, request);
        log.info("[POST] /api/visits/{}/treatment-results procedureResultId={}", visitId, result.getProcedureResultId());
        return ResponseEntity.status(201).body(new ApiResponse<>(true, "처치결과 등록 성공", result));
    }
}
