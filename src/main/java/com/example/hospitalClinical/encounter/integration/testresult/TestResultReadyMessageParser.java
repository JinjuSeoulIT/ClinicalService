package com.example.hospitalClinical.encounter.integration.testresult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class TestResultReadyMessageParser {

    private final ObjectMapper objectMapper;

    public Optional<TestResultReadyEntry> parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        try {
            JsonNode root = objectMapper.readTree(raw);
            JsonNode payload = unwrap(root);
            Long visitId = readLong(payload, "visitId", "visit_id", "clinicalId", "clinical_id");
            Long patientId = readLong(payload, "patientId", "patient_id");
            String resultId = readText(payload, "resultId", "result_id", "id");
            String resultType = readText(payload, "resultType", "result_type", "type");
            Long orderItemId = readLong(payload, "orderItemId", "order_item_id");
            if (resultId == null || resultId.isBlank()) {
                resultId = readText(payload, "examId", "exam_id", "testExecutionId", "test_execution_id");
            }
            if (visitId == null && patientId == null) {
                return Optional.empty();
            }
            if (resultId == null || resultId.isBlank()) {
                return Optional.empty();
            }
            String normalizedType = normalizeResultType(resultType, resultId);
            if (normalizedType == null || normalizedType.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(
                    TestResultReadyEntry.builder()
                            .visitId(visitId)
                            .patientId(patientId)
                            .resultId(resultId.trim())
                            .resultType(normalizedType)
                            .orderItemId(orderItemId)
                            .receivedAtEpochMilli(System.currentTimeMillis())
                            .build());
        } catch (Exception e) {
            log.warn("test-result-ready parse failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private static JsonNode unwrap(JsonNode root) {
        if (root == null || !root.isObject()) {
            return root;
        }
        JsonNode data = root.get("data");
        if (data != null && data.isObject()) {
            return data;
        }
        return root;
    }

    private static Long readLong(JsonNode node, String... fieldNames) {
        if (node == null) {
            return null;
        }
        for (String name : fieldNames) {
            JsonNode v = node.get(name);
            if (v == null || v.isNull()) {
                continue;
            }
            if (v.isIntegralNumber()) {
                return v.longValue();
            }
            if (v.isTextual()) {
                String t = v.asText().trim();
                if (t.isEmpty()) {
                    continue;
                }
                try {
                    return Long.parseLong(t);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private static String readText(JsonNode node, String... fieldNames) {
        if (node == null) {
            return null;
        }
        for (String name : fieldNames) {
            JsonNode v = node.get(name);
            if (v == null || v.isNull()) {
                continue;
            }
            if (v.isTextual()) {
                return v.asText();
            }
            if (v.isIntegralNumber()) {
                return String.valueOf(v.longValue());
            }
        }
        return null;
    }

    private static String normalizeResultType(String resultType, String resultId) {
        if (resultType != null && !resultType.isBlank()) {
            return resultType.trim().toUpperCase();
        }
        if (resultId == null) {
            return null;
        }
        String u = resultId.trim().toUpperCase();
        if (u.startsWith("IMG_") || u.contains("_IMG") || u.startsWith("IMG-")) {
            return "IMAGING";
        }
        if (u.startsWith("PATH_") || u.startsWith("PAT_") || u.contains("BIOPSY")) {
            return "PATHOLOGY";
        }
        if (u.startsWith("LAB_") || u.startsWith("SPEC_")) {
            return "SPECIMEN";
        }
        return null;
    }
}
