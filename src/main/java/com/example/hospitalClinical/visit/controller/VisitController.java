package com.example.hospitalClinical.visit.controller;

import com.example.hospitalClinical.common.response.ApiResponse;
import com.example.hospitalClinical.visit.dto.request.VisitCreateRequest;
import com.example.hospitalClinical.visit.dto.response.VisitResponse;
import com.example.hospitalClinical.visit.service.VisitService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visits")
public class VisitController {

    private final VisitService visitService;

    public VisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    @PostMapping
    public ApiResponse<VisitResponse> create(@RequestBody VisitCreateRequest req) {
        return ApiResponse.ok(VisitResponse.from(visitService.create(req)));
    }

    @GetMapping("/{visitId}")
    public ApiResponse<VisitResponse> get(@PathVariable Long visitId) {
        return ApiResponse.ok(VisitResponse.from(visitService.get(visitId)));
    }

    @GetMapping
    public ApiResponse<List<VisitResponse>> list(@RequestParam Long patientId) {
        List<VisitResponse> res = visitService.listByPatientId(patientId)
                .stream()
                .map(VisitResponse::from)
                .toList();
        return ApiResponse.ok(res);
    }
}