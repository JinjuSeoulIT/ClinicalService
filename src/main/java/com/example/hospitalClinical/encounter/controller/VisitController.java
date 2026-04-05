package com.example.hospitalClinical.encounter.controller;

import com.example.hospitalClinical.common.response.ApiResponse;
import com.example.hospitalClinical.documentation.dto.DrugSearchResult;
import com.example.hospitalClinical.documentation.dto.HiraProcedureSearchResult;
import com.example.hospitalClinical.documentation.service.ChartService;
import com.example.hospitalClinical.documentation.service.HiraProcedureSearchService;
import com.example.hospitalClinical.encounter.dto.VisitResponse;
import com.example.hospitalClinical.encounter.service.EncounterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
@RequestMapping("/api/visits")
public class VisitController {

    private final EncounterService encounterService;
    private final ChartService chartService;
    private final HiraProcedureSearchService hiraProcedureSearchService;

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

    @GetMapping("/{visitId}/drug-search")
    public ResponseEntity<ApiResponse<DrugSearchResult>> drugSearch(
            @PathVariable("visitId") Long visitId,
            @RequestParam(value = "itemName", required = false) String itemName,
            @RequestParam(value = "pageNo", required = false) Integer pageNo,
            @RequestParam(value = "numOfRows", required = false) Integer numOfRows) {
        log.info("[GET] /api/visits/{}/drug-search - 진료 맥락 약품 검색", visitId);
        encounterService.getVisit(visitId);
        DrugSearchResult result = chartService.searchDrugs(pageNo, numOfRows, itemName);
        return ResponseEntity.ok(new ApiResponse<>(true, "약품 검색 성공", result));
    }

    @GetMapping("/{visitId}/procedure-search")
    public ResponseEntity<ApiResponse<HiraProcedureSearchResult>> procedureSearch(
            @PathVariable("visitId") Long visitId,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "pageNo", required = false) Integer pageNo,
            @RequestParam(value = "numOfRows", required = false) Integer numOfRows) {
        log.info("[GET] /api/visits/{}/procedure-search - HIRA 진료수가 검색", visitId);
        encounterService.getVisit(visitId);
        String query = q != null ? q.trim() : "";
        int p = pageNo != null && pageNo > 0 ? pageNo : 1;
        int n = numOfRows != null && numOfRows > 0 ? Math.min(numOfRows, 100) : 20;
        if (query.length() < 2) {
            HiraProcedureSearchResult empty = HiraProcedureSearchResult.builder()
                    .resultCode("00")
                    .resultMsg("OK")
                    .pageNo(p)
                    .numOfRows(n)
                    .totalCount(0)
                    .items(java.util.List.of())
                    .build();
            return ResponseEntity.ok(new ApiResponse<>(true, "검색어 2자 이상 필요", empty));
        }
        try {
            HiraProcedureSearchResult result = hiraProcedureSearchService.searchProcedures(p, n, query);
            return ResponseEntity.ok(new ApiResponse<>(true, "진료수가 검색 성공", result));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(503)
                    .body(new ApiResponse<>(false, ex.getMessage(), null));
        }
    }
}
