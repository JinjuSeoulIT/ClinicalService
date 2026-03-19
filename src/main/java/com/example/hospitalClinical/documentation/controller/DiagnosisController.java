package com.example.hospitalClinical.documentation.controller;

import com.example.hospitalClinical.common.response.ApiResponse;
import com.example.hospitalClinical.documentation.dto.DiagnosisResponse;
import com.example.hospitalClinical.documentation.service.NoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = {"http://localhost:3001", "http://127.0.0.1:3001", "http://localhost:5173"})   //CORS정책
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/notes/{noteId}/diagnosis")
public class DiagnosisController {

    private final NoteService noteService;

    @PostMapping
    public ResponseEntity<ApiResponse<DiagnosisResponse>> create(
            @PathVariable("noteId") Long noteId,
            @RequestBody Map<String, String> body) {
        log.info("[POST] /api/notes/{}/diagnosis - 진단 등록", noteId);
        String patientCode = body != null ? body.get("patientCode") : null;
        String diagnosisCode = body != null ? body.get("diagnosisCode") : null;
        String description = body != null ? body.get("description") : null;
        DiagnosisResponse result = DiagnosisResponse.from(noteService.createDiagnosis(noteId, patientCode, diagnosisCode, description));
        return ResponseEntity.status(201).body(new ApiResponse<>(true, "진단 등록 성공", result));
    }

    @GetMapping("/{diagnosisId}")
    public ResponseEntity<ApiResponse<DiagnosisResponse>> get(
            @PathVariable("noteId") Long noteId,
            @PathVariable("diagnosisId") Long diagnosisId) {
        log.info("[GET] /api/notes/{}/diagnosis/{} - 진단 조회", noteId, diagnosisId);
        DiagnosisResponse result = DiagnosisResponse.from(noteService.getDiagnosis(diagnosisId));
        return ResponseEntity.ok(new ApiResponse<>(true, "진단 조회 성공", result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DiagnosisResponse>>> list(@PathVariable("noteId") Long noteId) {
        log.info("[GET] /api/notes/{}/diagnosis - 진단 목록 조회", noteId);
        List<DiagnosisResponse> list = noteService.listDiagnosisByNoteId(noteId).stream()
                .map(DiagnosisResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(true, "진단 목록 조회 성공", list));
    }
}
