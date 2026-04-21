package com.example.hospitalClinical.encounter.controller;

import com.example.hospitalClinical.common.response.ApiResponse;
import com.example.hospitalClinical.encounter.dto.TestResultReadyResponse;
import com.example.hospitalClinical.encounter.entity.Visit;
import com.example.hospitalClinical.encounter.integration.testresult.TestResultReadyStore;
import com.example.hospitalClinical.encounter.repository.VisitRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://127.0.0.1:3000", "http://127.0.0.1:3001", "http://localhost:5173", "http://192.168.1.64:3001"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clinical")
public class TestResultReadyController {

    private final VisitRepo visitRepo;
    private final TestResultReadyStore testResultReadyStore;

    @GetMapping("/visits/{visitId}/test-result-ready")
    public ResponseEntity<ApiResponse<TestResultReadyResponse>> get(
            @PathVariable("visitId") Long visitId,
            @RequestParam("patientId") Long patientId) {
        Visit visit =
                visitRepo.findById(visitId).orElseThrow(() -> new IllegalArgumentException("visit not found"));
        if (!visit.getPatientId().equals(patientId)) {
            throw new IllegalArgumentException("patient mismatch");
        }
        return testResultReadyStore
                .peek(visitId, patientId)
                .map(TestResultReadyResponse::from)
                .map(body -> ResponseEntity.ok(ApiResponse.ok(body)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.ok(null)));
    }

    @DeleteMapping("/visits/{visitId}/test-result-ready")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable("visitId") Long visitId,
            @RequestParam("patientId") Long patientId) {
        Visit visit =
                visitRepo.findById(visitId).orElseThrow(() -> new IllegalArgumentException("visit not found"));
        if (!visit.getPatientId().equals(patientId)) {
            throw new IllegalArgumentException("patient mismatch");
        }
        testResultReadyStore.clear(visitId, patientId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
