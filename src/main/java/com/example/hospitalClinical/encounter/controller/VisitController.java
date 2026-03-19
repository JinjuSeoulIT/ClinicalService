package com.example.hospitalClinical.encounter.controller;

import com.example.hospitalClinical.common.response.ApiResponse;
import com.example.hospitalClinical.encounter.dto.VisitResponse;
import com.example.hospitalClinical.encounter.service.EncounterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = {"http://localhost:3001", "http://127.0.0.1:3001", "http://localhost:5173"})
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/visits")
public class VisitController {

    private final EncounterService encounterService;

    @GetMapping("/{visitId}")
    public ResponseEntity<ApiResponse<VisitResponse>> get(@PathVariable("visitId") Long visitId) {
        log.info("[GET] /api/visits/{} - 진료 세션 조회", visitId);
        VisitResponse result = VisitResponse.from(encounterService.getVisit(visitId));
        return ResponseEntity.ok(new ApiResponse<>(true, "진료 세션 조회 성공", result));
    }

    @PatchMapping("/{visitId}/status")
    public ResponseEntity<ApiResponse<VisitResponse>> updateStatus(
            @PathVariable("visitId") Long visitId,
            @RequestBody Map<String, String> body) {
        log.info("[PATCH] /api/visits/{}/status - 진료 상태 변경", visitId);
        String status = body != null ? body.get("visitStatus") : null;
        VisitResponse result = VisitResponse.from(encounterService.updateVisitStatus(visitId, status));
        return ResponseEntity.ok(new ApiResponse<>(true, "진료 상태 변경 성공", result));
    }

    @PostMapping("/{visitId}/end")
    public ResponseEntity<ApiResponse<VisitResponse>> endVisit(@PathVariable("visitId") Long visitId) {
        log.info("[POST] /api/visits/{}/end - 진료 종료", visitId);
        VisitResponse result = VisitResponse.from(encounterService.endVisit(visitId));
        return ResponseEntity.ok(new ApiResponse<>(true, "진료 종료 성공", result));
    }
}
