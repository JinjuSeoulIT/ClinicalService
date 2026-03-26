package com.example.hospitalClinical.encounter.controller;

import com.example.hospitalClinical.common.response.ApiResponse;
import com.example.hospitalClinical.common.client.internal.reception.ReceptionClient;
import com.example.hospitalClinical.common.client.internal.reception.ReceptionResponse;
import com.example.hospitalClinical.encounter.dto.VisitCreateRequest;
import com.example.hospitalClinical.encounter.dto.VisitResponse;
import com.example.hospitalClinical.encounter.dto.VisitStartRequest;
import com.example.hospitalClinical.encounter.dto.VisitStartResponse;
import com.example.hospitalClinical.encounter.service.EncounterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://127.0.0.1:3000", "http://127.0.0.1:3001", "http://localhost:5173", "http://192.168.1.64:3001"})
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/clinical")
public class ClinicalController {

    private final EncounterService encounterService;
    private final ReceptionClient receptionClient;

    @GetMapping("/reception-queue")
    public ResponseEntity<ApiResponse<List<ReceptionResponse>>> receptionQueue(
            @RequestParam(value = "departmentId", required = false) Long departmentId,
            @RequestParam(value = "doctorId", required = false) Long doctorId,
            @RequestParam(value = "date", required = false) String date) {
        log.info("[GET] /api/clinical/reception-queue - 접수 대기열 조회");
        List<ReceptionResponse> list = receptionClient.getReceptionQueue(departmentId, doctorId, date);
        return ResponseEntity.ok(new ApiResponse<>(true, "접수 대기열 조회 성공", list));
    }

    @PostMapping("/start")
    public ResponseEntity<ApiResponse<VisitStartResponse>> start(@Valid @RequestBody VisitStartRequest request) {
        log.info("[POST] /api/clinical/start - 진료 시작");
        VisitStartResponse result = VisitStartResponse.from(encounterService.startVisit(request));
        return ResponseEntity.status(201).body(new ApiResponse<>(true, "진료 시작 성공", result));
    }

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
