package com.example.hospitalClinical.documentation.controller;

import com.example.hospitalClinical.common.response.ApiResponse;
import com.example.hospitalClinical.documentation.dto.VisitSoapDiagnosisAddRequest;
import com.example.hospitalClinical.documentation.dto.VisitSoapDiagnosisOrderRequest;
import com.example.hospitalClinical.documentation.dto.VisitSoapDiagnosisResponse;
import com.example.hospitalClinical.documentation.service.ChartService;
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
public class VisitSoapDiagnosisController {

    private final ChartService chartService;

    @GetMapping({"/api/visits/{visitId}/diagnoses", "/api/clinicals/{visitId}/diagnoses"})
    public ResponseEntity<ApiResponse<List<VisitSoapDiagnosisResponse>>> list(@PathVariable("visitId") Long visitId) {
        log.info("[GET] diagnoses visitId={}", visitId);
        List<VisitSoapDiagnosisResponse> list = chartService.listVisitSoapDiagnoses(visitId);
        return ResponseEntity.ok(new ApiResponse<>(true, "상병 목록 조회 성공", list));
    }

    @PostMapping({"/api/visits/{visitId}/diagnoses", "/api/clinicals/{visitId}/diagnoses"})
    public ResponseEntity<ApiResponse<VisitSoapDiagnosisResponse>> add(
            @PathVariable("visitId") Long visitId,
            @RequestBody VisitSoapDiagnosisAddRequest body) {
        log.info("[POST] diagnoses visitId={}", visitId);
        VisitSoapDiagnosisResponse result = chartService.addVisitSoapDiagnosis(visitId, body);
        return ResponseEntity.status(201).body(new ApiResponse<>(true, "상병 등록 성공", result));
    }

    @DeleteMapping({"/api/visits/{visitId}/diagnoses/{diagnosisId}", "/api/clinicals/{visitId}/diagnoses/{diagnosisId}"})
    public ResponseEntity<ApiResponse<Void>> remove(
            @PathVariable("visitId") Long visitId,
            @PathVariable("diagnosisId") Long diagnosisId) {
        log.info("[DELETE] diagnoses visitId={} diagnosisId={}", visitId, diagnosisId);
        chartService.removeVisitSoapDiagnosis(visitId, diagnosisId);
        return ResponseEntity.ok(new ApiResponse<>(true, "상병 삭제 성공", null));
    }

    @PatchMapping({"/api/visits/{visitId}/diagnoses/{diagnosisId}/main", "/api/clinicals/{visitId}/diagnoses/{diagnosisId}/main"})
    public ResponseEntity<ApiResponse<VisitSoapDiagnosisResponse>> setMain(
            @PathVariable("visitId") Long visitId,
            @PathVariable("diagnosisId") Long diagnosisId) {
        log.info("[PATCH] diagnoses main visitId={} diagnosisId={}", visitId, diagnosisId);
        VisitSoapDiagnosisResponse result = chartService.setMainVisitSoapDiagnosis(visitId, diagnosisId);
        return ResponseEntity.ok(new ApiResponse<>(true, "주진단 변경 성공", result));
    }

    @PutMapping({"/api/visits/{visitId}/diagnoses/order", "/api/clinicals/{visitId}/diagnoses/order"})
    public ResponseEntity<ApiResponse<Void>> reorder(
            @PathVariable("visitId") Long visitId,
            @RequestBody VisitSoapDiagnosisOrderRequest body) {
        log.info("[PUT] diagnoses order visitId={}", visitId);
        chartService.reorderVisitSoapDiagnoses(visitId, body.getDiagnosisIds());
        return ResponseEntity.ok(new ApiResponse<>(true, "상병 순서 변경 성공", null));
    }
}
