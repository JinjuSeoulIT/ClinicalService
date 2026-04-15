package com.example.hospitalClinical.documentation.controller;

import com.example.hospitalClinical.common.response.ApiResponse;
import com.example.hospitalClinical.documentation.dto.DrugSearchResult;
import com.example.hospitalClinical.documentation.service.DocumentationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/drug")
@CrossOrigin(origins = {
        "http://localhost:3000",
        "http://localhost:3001",
        "http://127.0.0.1:3000",
        "http://127.0.0.1:3001",
        "http://localhost:5173",
        "http://192.168.1.64:3001",
        "http://192.168.1.70:3001"
})
public class DrugController {

    private final DocumentationService documentationService;

    @GetMapping
    public ResponseEntity<ApiResponse<DrugSearchResult>> search(
            @RequestParam(value = "itemName", required = false) String itemName,
            @RequestParam(value = "itemSeq", required = false) String itemSeq,
            @RequestParam(value = "pageNo", required = false) Integer pageNo,
            @RequestParam(value = "numOfRows", required = false) Integer numOfRows) {
        DrugSearchResult result = documentationService.searchDrugs(pageNo, numOfRows, itemName, itemSeq);
        return ResponseEntity.ok(ApiResponse.ok("약품 검색 성공", result));
    }
}
