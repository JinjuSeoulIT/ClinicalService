package com.example.hospitalClinical.common.client.external.disease;

import com.example.hospitalClinical.documentation.dto.StandardDiagnosisItemDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DiseaseDissNameCodeJsonParser {

    private final ObjectMapper objectMapper;

    public List<StandardDiagnosisItemDto> parseDissNameCodeList(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode envelope = root.has("response") ? root.get("response") : root;
            JsonNode body = envelope.path("body");
            return mapItems(body.path("items"));
        } catch (Exception e) {
            throw new IllegalStateException("공공데이터 질병명칭 JSON 파싱 실패: " + e.getMessage(), e);
        }
    }

    private List<StandardDiagnosisItemDto> mapItems(JsonNode itemsNode) {
        List<StandardDiagnosisItemDto> out = new ArrayList<>();
        if (itemsNode == null || itemsNode.isMissingNode() || itemsNode.isNull()) {
            return out;
        }
        if (itemsNode.isArray()) {
            for (JsonNode n : itemsNode) {
                addIfPresent(out, n);
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
                    addIfPresent(out, n);
                }
            } else {
                addIfPresent(out, item);
            }
        }
        return out;
    }

    private static void addIfPresent(List<StandardDiagnosisItemDto> out, JsonNode n) {
        if (n == null || n.isNull() || n.isMissingNode()) {
            return;
        }
        String code = firstNonBlank(
                text(n, "sickCd"),
                text(n, "dissCd"),
                text(n, "diseaseCd"),
                text(n, "code"));
        String name = firstNonBlank(
                text(n, "sickNm"),
                text(n, "dissNm"),
                text(n, "diseaseNm"),
                text(n, "name"));
        if (!code.isEmpty() || !name.isEmpty()) {
            out.add(new StandardDiagnosisItemDto(code, name));
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.path(field);
        return v.isMissingNode() || v.isNull() ? "" : v.asText("").trim();
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String s : values) {
            if (s != null && !s.isBlank()) {
                return s;
            }
        }
        return "";
    }
}
