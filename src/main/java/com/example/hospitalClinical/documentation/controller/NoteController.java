package com.example.hospitalClinical.documentation.controller;

import com.example.hospitalClinical.common.response.ApiResponse;
import com.example.hospitalClinical.documentation.dto.NoteResponse;
import com.example.hospitalClinical.documentation.service.ChartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = {"http://localhost:3001", "http://127.0.0.1:3001", "http://localhost:5173", "http://192.168.1.64:3001"})
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/visits/{visitId}/notes")
public class NoteController {

    private final ChartService chartService;

    @PostMapping
    public ResponseEntity<ApiResponse<NoteResponse>> create(@PathVariable("visitId") Long visitId) {
        log.info("[POST] /api/visits/{}/notes - 진료기록 등록", visitId);
        NoteResponse result = NoteResponse.from(chartService.createNote(visitId));
        return ResponseEntity.status(201).body(new ApiResponse<>(true, "진료기록 등록 성공", result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<NoteResponse>> getByVisit(@PathVariable("visitId") Long visitId) {
        log.info("[GET] /api/visits/{}/notes - 진료기록 조회(Visit기준)", visitId);
        return chartService.findNoteByVisitId(visitId)
                .map(n -> ResponseEntity.ok(new ApiResponse<>(true, "진료기록 조회 성공", NoteResponse.from(n))))
                .orElse(ResponseEntity.status(404).body(new ApiResponse<>(false, "진료기록을 찾을 수 없습니다.", null)));
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<NoteResponse>>> list(@PathVariable("visitId") Long visitId) {
        log.info("[GET] /api/visits/{}/notes/list - 진료기록 목록 조회", visitId);
        List<NoteResponse> list = chartService.listNotesByVisitId(visitId).stream()
                .map(NoteResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(true, "진료기록 목록 조회 성공", list));
    }

    @PatchMapping("/{noteId}")
    public ResponseEntity<ApiResponse<NoteResponse>> update(
            @PathVariable("visitId") Long visitId,
            @PathVariable("noteId") Long noteId,
            @RequestBody Map<String, String> body) {
        log.info("[PATCH] /api/visits/{}/notes/{} - 진료기록 저장", visitId, noteId);
        NoteResponse result = NoteResponse.from(chartService.updateNote(
                noteId,
                body.get("chiefComplaint"),
                body.get("presentIllness"),
                body.get("assessment"),
                body.get("plan"),
                body.get("memo"),
                body.get("status")
        ));
        return ResponseEntity.ok(new ApiResponse<>(true, "진료기록 저장 성공", result));
    }
}
