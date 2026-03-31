package com.example.hospitalClinical.documentation.controller;

import com.example.hospitalClinical.common.response.ApiResponse;
import com.example.hospitalClinical.documentation.dto.SoapDxOrderRequest;
import com.example.hospitalClinical.documentation.dto.SoapDxRequest;
import com.example.hospitalClinical.documentation.dto.SoapDxResponse;
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
public class SoapDxController {

    private final ChartService chartService;

    @GetMapping({"/api/visits/{visitId}/diagnoses", "/api/clinicals/{visitId}/diagnoses"})
    public ResponseEntity<ApiResponse<List<SoapDxResponse>>> list(@PathVariable("visitId") Long visitId) {
        log.info("[GET] diagnoses visitId={}", visitId);
        List<SoapDxResponse> list = chartService.listSoapDx(visitId);
        return ResponseEntity.ok(new ApiResponse<>(true, "상병 목록 조회 성공", list));
    }

    @PostMapping({"/api/visits/{visitId}/diagnoses", "/api/clinicals/{visitId}/diagnoses"})
    public ResponseEntity<ApiResponse<SoapDxResponse>> add(
            @PathVariable("visitId") Long visitId,
            @RequestBody SoapDxRequest body) {
        log.info("[POST] diagnoses visitId={}", visitId);
        SoapDxResponse result = chartService.addSoapDx(visitId, body);
        return ResponseEntity.status(201).body(new ApiResponse<>(true, "상병 등록 성공", result));
    }

    @DeleteMapping({"/api/visits/{visitId}/diagnoses/{diagnosisId}", "/api/clinicals/{visitId}/diagnoses/{diagnosisId}"})
    public ResponseEntity<ApiResponse<Void>> remove(
            @PathVariable("visitId") Long visitId,
            @PathVariable("diagnosisId") Long diagnosisId) {
        log.info("[DELETE] diagnoses visitId={} diagnosisId={}", visitId, diagnosisId);
        chartService.removeSoapDx(visitId, diagnosisId);
        return ResponseEntity.ok(new ApiResponse<>(true, "상병 삭제 성공", null));
    }

    @PatchMapping({"/api/visits/{visitId}/diagnoses/{diagnosisId}/main", "/api/clinicals/{visitId}/diagnoses/{diagnosisId}/main"})
    public ResponseEntity<ApiResponse<SoapDxResponse>> setMain(
            @PathVariable("visitId") Long visitId,
            @PathVariable("diagnosisId") Long diagnosisId) {
        log.info("[PATCH] diagnoses main visitId={} diagnosisId={}", visitId, diagnosisId);
        SoapDxResponse result = chartService.setMainSoapDx(visitId, diagnosisId);
        return ResponseEntity.ok(new ApiResponse<>(true, "주진단 변경 성공", result));
    }

    @PutMapping({"/api/visits/{visitId}/diagnoses/order", "/api/clinicals/{visitId}/diagnoses/order"})
    public ResponseEntity<ApiResponse<Void>> reorder(
            @PathVariable("visitId") Long visitId,
            @RequestBody SoapDxOrderRequest body) {
        log.info("[PUT] diagnoses order visitId={}", visitId);
        chartService.reorderSoapDx(visitId, body.getDiagnosisIds());
        return ResponseEntity.ok(new ApiResponse<>(true, "상병 순서 변경 성공", null));
    }
}
