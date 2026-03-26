package com.example.hospitalClinical.documentation.controller;

import com.example.hospitalClinical.common.response.ApiResponse;
import com.example.hospitalClinical.documentation.dto.NoteHistoryResponse;
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
@RequestMapping("/api/notes/{noteId}/history")
public class NoteHistoryController {

    private final ChartService chartService;

    @PostMapping
    public ResponseEntity<ApiResponse<NoteHistoryResponse>> create(
            @PathVariable("noteId") Long noteId,
            @RequestBody Map<String, String> body) {
        log.info("[POST] /api/notes/{}/history - 진료기록 이력 등록", noteId);
        String changeType = body != null ? body.get("changeType") : null;
        Long changedBy = body != null && body.get("changedBy") != null ? Long.parseLong(body.get("changedBy")) : null;
        NoteHistoryResponse result = NoteHistoryResponse.from(chartService.createNoteHistory(noteId, changeType, changedBy));
        return ResponseEntity.status(201).body(new ApiResponse<>(true, "진료기록 이력 등록 성공", result));
    }

    @GetMapping("/{historyId}")
    public ResponseEntity<ApiResponse<NoteHistoryResponse>> get(
            @PathVariable("noteId") Long noteId,
            @PathVariable("historyId") Long historyId) {
        log.info("[GET] /api/notes/{}/history/{} - 진료기록 이력 조회", noteId, historyId);
        NoteHistoryResponse result = NoteHistoryResponse.from(chartService.getNoteHistory(historyId));
        return ResponseEntity.ok(new ApiResponse<>(true, "진료기록 이력 조회 성공", result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NoteHistoryResponse>>> list(@PathVariable("noteId") Long noteId) {
        log.info("[GET] /api/notes/{}/history - 진료기록 이력 목록 조회", noteId);
        List<NoteHistoryResponse> list = chartService.listNoteHistoryByNoteId(noteId).stream()
                .map(NoteHistoryResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(true, "진료기록 이력 목록 조회 성공", list));
    }
}
