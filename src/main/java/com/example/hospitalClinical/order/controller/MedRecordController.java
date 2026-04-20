package com.example.hospitalClinical.order.controller;

import com.example.hospitalClinical.common.response.ApiResponse;
import com.example.hospitalClinical.order.dto.MedicationRecordCreateRequest;
import com.example.hospitalClinical.order.dto.MedicationRecordResponse;
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
@RequestMapping("/api/visits/{visitId}/medication-records")
public class MedRecordController {

    private final OrderVisitService orderVisitService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MedicationRecordResponse>>> list(
            @PathVariable("visitId") Long visitId) {
        log.info("[GET] /api/visits/{}/medication-records", visitId);
        List<MedicationRecordResponse> list = orderVisitService.listMedicationRecordsByVisit(visitId);
        return ResponseEntity.ok(new ApiResponse<>(true, "투약기록 목록 조회 성공", list));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MedicationRecordResponse>> create(
            @PathVariable("visitId") Long visitId,
            @RequestBody @Valid MedicationRecordCreateRequest request) {
        MedicationRecordResponse result = orderVisitService.createMedicationRecord(visitId, request);
        log.info("[POST] /api/visits/{}/medication-records medicationId={}", visitId, result.getMedicationId());
        return ResponseEntity.status(201).body(new ApiResponse<>(true, "투약기록 등록 성공", result));
    }
}
