package com.example.hospitalClinical.common.client.external.disease;

import com.example.hospitalClinical.documentation.dto.StandardDiagnosisItemDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DiseaseDissNameCodeJsonParser {

    private final ObjectMapper objectMapper;

    public List<StandardDiagnosisItemDto> parseDissNameCodeList(String body) {
        if (body == null || body.isBlank()) {
            return List.of();
        }
        String lead = body.stripLeading();
        if (lead.startsWith("<")) {
            return parseDissNameCodeListXml(body);
        }
        return parseDissNameCodeListJson(body);
    }

    private List<StandardDiagnosisItemDto> parseDissNameCodeListJson(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode envelope = root.has("response") ? root.get("response") : root;
            JsonNode body = envelope.path("body");
            return mapItems(body.path("items"));
        } catch (Exception e) {
            throw new IllegalStateException("공공데이터 질병명칭 JSON 파싱 실패: " + e.getMessage(), e);
        }
    }

    private List<StandardDiagnosisItemDto> parseDissNameCodeListXml(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));
            NodeList itemNodes = doc.getElementsByTagNameNS("*", "item");
            if (itemNodes.getLength() == 0) {
                itemNodes = doc.getElementsByTagName("item");
            }
            List<StandardDiagnosisItemDto> out = new ArrayList<>();
            for (int i = 0; i < itemNodes.getLength(); i++) {
                if (!(itemNodes.item(i) instanceof Element el)) {
                    continue;
                }
                String code = firstNonBlank(
                        elementText(el, "sickCd"),
                        elementText(el, "dissCd"),
                        elementText(el, "diseaseCd"),
                        elementText(el, "code"));
                String name = firstNonBlank(
                        elementText(el, "sickNm"),
                        elementText(el, "dissNm"),
                        elementText(el, "diseaseNm"),
                        elementText(el, "name"));
                if (!code.isEmpty() || !name.isEmpty()) {
                    out.add(new StandardDiagnosisItemDto(code, name));
                }
            }
            return out;
        } catch (Exception e) {
            throw new IllegalStateException("공공데이터 질병명칭 XML 파싱 실패: " + e.getMessage(), e);
        }
    }

    private static String elementText(Element parent, String tag) {
        NodeList nl = parent.getElementsByTagNameNS("*", tag);
        if (nl.getLength() == 0) {
            nl = parent.getElementsByTagName(tag);
        }
        if (nl.getLength() == 0) {
            return "";
        }
        return nl.item(0).getTextContent() != null ? nl.item(0).getTextContent().trim() : "";
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
