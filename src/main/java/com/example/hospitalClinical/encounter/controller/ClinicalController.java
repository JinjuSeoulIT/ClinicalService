package com.example.hospitalClinical.encounter.controller;

import com.example.hospitalClinical.common.response.ApiResponse;
import com.example.hospitalClinical.encounter.dto.VisitCreateRequest;
import com.example.hospitalClinical.encounter.dto.VisitResponse;
import com.example.hospitalClinical.encounter.service.EncounterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = {"http://localhost:3001", "http://127.0.0.1:3001", "http://localhost:5173"})
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/clinical")
public class ClinicalController {

    private final EncounterService encounterService;

    @PostMapping
    public ResponseEntity<ApiResponse<VisitResponse>> create(@RequestBody VisitCreateRequest request) {
        log.info("[POST] /api/clinical - 진료 세션 등록");
        VisitResponse result = VisitResponse.from(encounterService.createVisit(request));
        return ResponseEntity.status(201).body(new ApiResponse<>(true, "진료 세션 등록 성공", result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VisitResponse>>> list(
            @RequestParam(value = "patientId", required = false) Long patientId,
            @RequestParam(value = "receptionId", required = false) Long receptionId,
            @RequestParam(value = "clinicalStatus", required = false) String visitStatus) {
        log.info("[GET] /api/clinical - 진료 목록 조회");
        List<VisitResponse> list;
        if (patientId != null) {
            list = encounterService.listByPatientId(patientId).stream().map(VisitResponse::from).collect(Collectors.toList());
        } else if (receptionId != null) {
            list = encounterService.listByReceptionId(receptionId).stream().map(VisitResponse::from).collect(Collectors.toList());
        } else if (visitStatus != null) {
            list = encounterService.listByStatus(visitStatus).stream().map(VisitResponse::from).collect(Collectors.toList());
        } else {
            list = encounterService.listAll().stream().map(VisitResponse::from).collect(Collectors.toList());
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "진료 목록 조회 성공", list));
    }
}
