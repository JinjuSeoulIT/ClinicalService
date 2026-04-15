package com.example.hospitalClinical.documentation.controller;

import com.example.hospitalClinical.common.response.ApiResponse;
import com.example.hospitalClinical.documentation.dto.SoapRxRequest;
import com.example.hospitalClinical.documentation.dto.SoapRxResponse;
import com.example.hospitalClinical.documentation.service.DocumentationService;
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
public class SoapRxController {

    private final DocumentationService documentationService;

    @GetMapping({"/api/visits/{visitId}/prescriptions", "/api/clinicals/{visitId}/prescriptions"})
    public ResponseEntity<ApiResponse<List<SoapRxResponse>>> list(@PathVariable("visitId") Long visitId) {
        log.info("[GET] prescriptions visitId={}", visitId);
        List<SoapRxResponse> list = documentationService.listSoapRx(visitId);
        return ResponseEntity.ok(new ApiResponse<>(true, "처방 목록 조회 성공", list));
    }

    @PostMapping({"/api/visits/{visitId}/prescriptions", "/api/clinicals/{visitId}/prescriptions"})
    public ResponseEntity<ApiResponse<SoapRxResponse>> add(
            @PathVariable("visitId") Long visitId,
            @RequestBody SoapRxRequest body) {
        log.info("[POST] prescriptions visitId={}", visitId);
        SoapRxResponse result = documentationService.addSoapRx(visitId, body);
        return ResponseEntity.status(201).body(new ApiResponse<>(true, "처방 등록 성공", result));
    }

    @DeleteMapping({"/api/visits/{visitId}/prescriptions/{prescriptionId}",
            "/api/clinicals/{visitId}/prescriptions/{prescriptionId}"})
    public ResponseEntity<ApiResponse<Void>> remove(
            @PathVariable("visitId") Long visitId,
            @PathVariable("prescriptionId") Long prescriptionId) {
        log.info("[DELETE] prescriptions visitId={} prescriptionId={}", visitId, prescriptionId);
        documentationService.removeSoapRx(visitId, prescriptionId);
        return ResponseEntity.ok(new ApiResponse<>(true, "처방 삭제 성공", null));
    }
}
