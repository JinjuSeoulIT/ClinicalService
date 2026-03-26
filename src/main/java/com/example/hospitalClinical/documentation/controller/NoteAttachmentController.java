package com.example.hospitalClinical.documentation.controller;

import com.example.hospitalClinical.common.response.ApiResponse;
import com.example.hospitalClinical.documentation.dto.NoteAttachmentResponse;
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
@RequestMapping("/api/notes/{noteId}/attachments")
public class NoteAttachmentController {

    private final ChartService chartService;

    @PostMapping
    public ResponseEntity<ApiResponse<NoteAttachmentResponse>> create(
            @PathVariable("noteId") Long noteId,
            @RequestBody Map<String, String> body) {
        log.info("[POST] /api/notes/{}/attachments - 첨부 등록", noteId);
        String fileName = body != null ? body.get("fileName") : null;
        String filePath = body != null ? body.get("filePath") : null;
        String fileType = body != null ? body.get("fileType") : null;
        NoteAttachmentResponse result = NoteAttachmentResponse.from(chartService.createAttachment(noteId, fileName, filePath, fileType));
        return ResponseEntity.status(201).body(new ApiResponse<>(true, "첨부 등록 성공", result));
    }

    @GetMapping("/{attachmentId}")
    public ResponseEntity<ApiResponse<NoteAttachmentResponse>> get(
            @PathVariable("noteId") Long noteId,
            @PathVariable("attachmentId") Long attachmentId) {
        log.info("[GET] /api/notes/{}/attachments/{} - 첨부 조회", noteId, attachmentId);
        NoteAttachmentResponse result = NoteAttachmentResponse.from(chartService.getAttachment(attachmentId));
        return ResponseEntity.ok(new ApiResponse<>(true, "첨부 조회 성공", result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NoteAttachmentResponse>>> list(@PathVariable("noteId") Long noteId) {
        log.info("[GET] /api/notes/{}/attachments - 첨부 목록 조회", noteId);
        List<NoteAttachmentResponse> list = chartService.listAttachmentByNoteId(noteId).stream()
                .map(NoteAttachmentResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(true, "첨부 목록 조회 성공", list));
    }
}
