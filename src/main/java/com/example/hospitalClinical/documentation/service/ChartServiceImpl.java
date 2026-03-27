package com.example.hospitalClinical.documentation.service;

import com.example.hospitalClinical.common.client.external.disease.DiseaseApiClient;
import com.example.hospitalClinical.common.client.external.disease.DiseaseDissNameCodeJsonParser;
import com.example.hospitalClinical.common.client.external.drug.DrugApiClient;
import com.example.hospitalClinical.common.exception.BusinessException;
import com.example.hospitalClinical.common.exception.ErrorCode;
import com.example.hospitalClinical.documentation.dto.DrugItemDto;
import com.example.hospitalClinical.documentation.dto.DrugSearchResult;
import com.example.hospitalClinical.documentation.dto.StandardDiagnosisItemDto;
import com.example.hospitalClinical.documentation.dto.VisitSoapDiagnosisAddRequest;
import com.example.hospitalClinical.documentation.dto.VisitSoapDiagnosisResponse;
import com.example.hospitalClinical.documentation.entity.Diagnosis;
import com.example.hospitalClinical.documentation.entity.Note;
import com.example.hospitalClinical.documentation.entity.NoteAttachment;
import com.example.hospitalClinical.documentation.entity.NoteHistory;
import com.example.hospitalClinical.documentation.entity.VisitSoapDiagnosis;
import com.example.hospitalClinical.documentation.exception.NoteNotFoundException;
import com.example.hospitalClinical.documentation.repository.DiagnosisRepo;
import com.example.hospitalClinical.documentation.repository.NoteAttachmentRepo;
import com.example.hospitalClinical.documentation.repository.NoteHistoryRepo;
import com.example.hospitalClinical.documentation.repository.NoteRepo;
import com.example.hospitalClinical.documentation.repository.VisitSoapDiagnosisRepo;
import com.example.hospitalClinical.encounter.repository.VisitRepo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChartServiceImpl implements ChartService {

    private final NoteRepo noteRepo;
    private final DiagnosisRepo diagnosisRepo;
    private final NoteHistoryRepo noteHistoryRepo;
    private final NoteAttachmentRepo noteAttachmentRepo;
    private final VisitRepo visitRepo;
    private final VisitSoapDiagnosisRepo visitSoapDiagnosisRepo;
    private final DrugApiClient drugApiClient;
    private final DiseaseApiClient diseaseApiClient;
    private final DiseaseDissNameCodeJsonParser diseaseDissNameCodeJsonParser;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public Note createNote(Long visitId) {
        if (!visitRepo.existsById(visitId)) {
            throw new IllegalArgumentException("Visit not found: " + visitId);
        }
        return noteRepo.save(Note.create(visitId));
    }

    @Override
    public Note getNote(Long noteId) {
        return noteRepo.findById(noteId).orElseThrow(NoteNotFoundException::new);
    }

    @Override
    public Note getNoteByVisitId(Long visitId) {
        return noteRepo.findByVisitId(visitId).orElseThrow(NoteNotFoundException::new);
    }

    @Override
    public Optional<Note> findNoteByVisitId(Long visitId) {
        return noteRepo.findByVisitId(visitId);
    }

    @Override
    public List<Note> listNotesByVisitId(Long visitId) {
        return noteRepo.findByVisitIdOrderByCreatedAtDesc(visitId);
    }

    @Override
    @Transactional
    public Note updateNote(Long noteId, String chiefComplaint, String presentIllness, String assessment, String plan,
                           String memo, String status) {
        Note n = getNote(noteId);
        if (chiefComplaint != null) {
            n.setChiefComplaint(chiefComplaint);
        }
        if (presentIllness != null) {
            n.setPresentIllness(presentIllness);
        }
        if (assessment != null) {
            n.setAssessment(assessment);
        }
        if (plan != null) {
            n.setPlan(plan);
        }
        if (memo != null) {
            n.setMemo(memo);
        }
        if (status != null) {
            n.setStatus(status);
        }
        return noteRepo.save(n);
    }

    @Override
    @Transactional
    public Diagnosis createDiagnosis(Long noteId, String patientCode, String diagnosisCode, String description) {
        if (!noteRepo.existsById(noteId)) {
            throw new NoteNotFoundException();
        }
        return diagnosisRepo.save(Diagnosis.create(noteId, patientCode, diagnosisCode, description));
    }

    @Override
    public Diagnosis getDiagnosis(Long diagnosisId) {
        return diagnosisRepo.findById(diagnosisId)
                .orElseThrow(() -> new IllegalArgumentException("Diagnosis not found: " + diagnosisId));
    }

    @Override
    public List<Diagnosis> listDiagnosisByNoteId(Long noteId) {
        return diagnosisRepo.findByNoteIdOrderByCreatedAtDesc(noteId);
    }

    @Override
    @Transactional
    public NoteHistory createNoteHistory(Long noteId, String changeType, Long changedBy) {
        return noteHistoryRepo.save(NoteHistory.create(noteId, changeType, changedBy));
    }

    @Override
    public NoteHistory getNoteHistory(Long historyId) {
        return noteHistoryRepo.findById(historyId)
                .orElseThrow(() -> new IllegalArgumentException("NoteHistory not found: " + historyId));
    }

    @Override
    public List<NoteHistory> listNoteHistoryByNoteId(Long noteId) {
        return noteHistoryRepo.findByNoteIdOrderByChangedAtDesc(noteId);
    }

    @Override
    @Transactional
    public NoteAttachment createAttachment(Long noteId, String fileName, String filePath, String fileType) {
        if (!noteRepo.existsById(noteId)) {
            throw new NoteNotFoundException();
        }
        return noteAttachmentRepo.save(NoteAttachment.create(noteId, fileName, filePath, fileType));
    }

    @Override
    public NoteAttachment getAttachment(Long attachmentId) {
        return noteAttachmentRepo.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("NoteAttachment not found: " + attachmentId));
    }

    @Override
    public List<NoteAttachment> listAttachmentByNoteId(Long noteId) {
        return noteAttachmentRepo.findByNoteIdOrderByCreatedAtDesc(noteId);
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public DrugSearchResult searchDrugs(Integer pageNo, Integer numOfRows, String itemName) {
        int p = pageNo != null && pageNo > 0 ? pageNo : 1;
        int n = numOfRows != null && numOfRows > 0 ? Math.min(numOfRows, 100) : 10;
        String json = drugApiClient.fetchEasyDrugList(p, n, itemName);
        return parseDrugSearchResult(json, p, n);
    }

    @Override
    public List<VisitSoapDiagnosisResponse> listVisitSoapDiagnoses(Long visitId) {
        assertVisit(visitId);
        return visitSoapDiagnosisRepo.findByVisitIdOrderBySortOrderAscDiagnosisIdAsc(visitId).stream()
                .map(VisitSoapDiagnosisResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VisitSoapDiagnosisResponse addVisitSoapDiagnosis(Long visitId, VisitSoapDiagnosisAddRequest request) {
        assertVisit(visitId);
        boolean asMain = Boolean.TRUE.equals(request != null ? request.getMain() : null);
        if (asMain) {
            List<VisitSoapDiagnosis> existing = visitSoapDiagnosisRepo.findByVisitIdOrderBySortOrderAscDiagnosisIdAsc(visitId);
            boolean hasMain = existing.stream().anyMatch(d -> "Y".equals(d.getMainYn()));
            if (hasMain) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "이미 주상병이 있습니다.");
            }
        } else {
            List<VisitSoapDiagnosis> existing = visitSoapDiagnosisRepo.findByVisitIdOrderBySortOrderAscDiagnosisIdAsc(visitId);
            if (existing.isEmpty()) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "먼저 주상병을 등록하세요.");
            }
        }
        int nextOrder = visitSoapDiagnosisRepo.findByVisitIdOrderBySortOrderAscDiagnosisIdAsc(visitId).stream()
                .mapToInt(VisitSoapDiagnosis::getSortOrder)
                .max()
                .orElse(-1) + 1;
        String code = request != null && request.getDxCode() != null ? request.getDxCode().trim() : null;
        String name = request != null && request.getDxName() != null ? request.getDxName().trim() : null;
        if ((code == null || code.isEmpty()) && (name == null || name.isEmpty())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "상병기호 또는 상병명이 필요합니다.");
        }
        if (asMain) {
            visitSoapDiagnosisRepo.findByVisitIdOrderBySortOrderAscDiagnosisIdAsc(visitId)
                    .forEach(d -> d.setMainYn("N"));
        }
        VisitSoapDiagnosis saved = visitSoapDiagnosisRepo.save(
                VisitSoapDiagnosis.create(visitId, emptyToNull(code), emptyToNull(name), asMain, nextOrder));
        return VisitSoapDiagnosisResponse.from(saved);
    }

    @Override
    @Transactional
    public void removeVisitSoapDiagnosis(Long visitId, Long diagnosisId) {
        assertVisit(visitId);
        VisitSoapDiagnosis d = visitSoapDiagnosisRepo.findByDiagnosisIdAndVisitId(diagnosisId, visitId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST, "상병을 찾을 수 없습니다."));
        visitSoapDiagnosisRepo.delete(d);
    }

    @Override
    @Transactional
    public VisitSoapDiagnosisResponse setMainVisitSoapDiagnosis(Long visitId, Long diagnosisId) {
        assertVisit(visitId);
        VisitSoapDiagnosis target = visitSoapDiagnosisRepo.findByDiagnosisIdAndVisitId(diagnosisId, visitId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST, "상병을 찾을 수 없습니다."));
        visitSoapDiagnosisRepo.findByVisitIdOrderBySortOrderAscDiagnosisIdAsc(visitId)
                .forEach(d -> d.setMainYn("N"));
        target.setMainYn("Y");
        return VisitSoapDiagnosisResponse.from(visitSoapDiagnosisRepo.save(target));
    }

    @Override
    @Transactional
    public void reorderVisitSoapDiagnoses(Long visitId, List<Long> diagnosisIds) {
        assertVisit(visitId);
        if (diagnosisIds == null || diagnosisIds.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "순서 목록이 비었습니다.");
        }
        if (new HashSet<>(diagnosisIds).size() != diagnosisIds.size()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "상병 ID가 중복되었습니다.");
        }
        List<VisitSoapDiagnosis> rows = visitSoapDiagnosisRepo.findByVisitIdOrderBySortOrderAscDiagnosisIdAsc(visitId);
        if (rows.size() != diagnosisIds.size()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "상병 개수가 일치하지 않습니다.");
        }
        var idSet = new HashSet<Long>();
        for (VisitSoapDiagnosis r : rows) {
            idSet.add(r.getDiagnosisId());
        }
        for (Long id : diagnosisIds) {
            if (!idSet.contains(id)) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "잘못된 상병 ID가 포함되었습니다.");
            }
        }
        for (int i = 0; i < diagnosisIds.size(); i++) {
            Long id = diagnosisIds.get(i);
            VisitSoapDiagnosis d = rows.stream()
                    .filter(x -> x.getDiagnosisId().equals(id))
                    .findFirst()
                    .orElseThrow();
            d.setSortOrder(i);
        }
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<StandardDiagnosisItemDto> searchStandardDiagnosisMasters(String query, Integer pageNo,
                                                                         Integer numOfRows) {
        if (!StringUtils.hasText(query)) {
            return List.of();
        }
        int p = pageNo != null && pageNo > 0 ? pageNo : 1;
        int n = numOfRows != null && numOfRows > 0 ? Math.min(numOfRows, 100) : 20;
        String json = diseaseApiClient.fetchDissNameCodeList(p, n, "SICK_NM", query.trim());
        return diseaseDissNameCodeJsonParser.parseDissNameCodeList(json);
    }

    private DrugSearchResult parseDrugSearchResult(String json, int requestedPage, int requestedNumOfRows) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode envelope = root.has("response") ? root.get("response") : root;
            JsonNode header = envelope.path("header");
            JsonNode body = envelope.path("body");

            String resultCode = textOrEmpty(header, "resultCode");
            String resultMsg = textOrEmpty(header, "resultMsg");
            int page = body.path("pageNo").asInt(requestedPage);
            int rows = body.path("numOfRows").asInt(requestedNumOfRows);
            int total = body.path("totalCount").asInt(0);

            List<DrugItemDto> items = extractDrugItems(body.path("items"));

            return DrugSearchResult.builder()
                    .resultCode(resultCode)
                    .resultMsg(resultMsg)
                    .pageNo(page)
                    .numOfRows(rows)
                    .totalCount(total)
                    .items(items)
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("공공데이터 약품 JSON 파싱 실패: " + e.getMessage(), e);
        }
    }

    private static String textOrEmpty(JsonNode node, String field) {
        JsonNode v = node.path(field);
        return v.isMissingNode() || v.isNull() ? "" : v.asText("");
    }

    private List<DrugItemDto> extractDrugItems(JsonNode itemsNode)
            throws com.fasterxml.jackson.core.JsonProcessingException {
        List<DrugItemDto> out = new ArrayList<>();
        if (itemsNode == null || itemsNode.isMissingNode() || itemsNode.isNull()) {
            return out;
        }
        if (itemsNode.isArray()) {
            for (JsonNode n : itemsNode) {
                out.add(objectMapper.treeToValue(n, DrugItemDto.class));
            }
            return out;
        }
        if (itemsNode.isObject()) {
            JsonNode item = itemsNode.get("item");
            if (item == null) {
                return out;
            }
            if (item.isArray()) {
                for (JsonNode n : item) {
                    out.add(objectMapper.treeToValue(n, DrugItemDto.class));
                }
            } else {
                out.add(objectMapper.treeToValue(item, DrugItemDto.class));
            }
        }
        return out;
    }

    private void assertVisit(Long visitId) {
        if (visitId == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "visitId가 필요합니다.");
        }
        if (!visitRepo.existsById(visitId)) {
            throw new BusinessException(ErrorCode.VISIT_NOT_FOUND);
        }
    }

    private static String emptyToNull(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        return s;
    }
}
