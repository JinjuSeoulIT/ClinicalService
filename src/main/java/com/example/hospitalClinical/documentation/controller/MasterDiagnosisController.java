package com.example.hospitalClinical.documentation.controller;

import com.example.hospitalClinical.common.response.ApiResponse;
import com.example.hospitalClinical.documentation.dto.StandardDiagnosisItemDto;
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
@RequestMapping("/api/master-diagnoses")
public class MasterDiagnosisController {

    private final ChartService chartService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<StandardDiagnosisItemDto>>> search(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "pageNo", required = false) Integer pageNo,
            @RequestParam(value = "numOfRows", required = false) Integer numOfRows,
            @RequestParam(value = "diseaseType", required = false) String diseaseType) {
        log.info("[GET] /api/master-diagnoses query={} pageNo={} numOfRows={} diseaseType={}", query, pageNo,
                numOfRows, diseaseType);
        List<StandardDiagnosisItemDto> list =
                chartService.searchStandardDiagnosisMasters(query, pageNo, numOfRows, diseaseType);
        return ResponseEntity.ok(ApiResponse.ok("표준 상병 검색 성공", list));
    }
}
