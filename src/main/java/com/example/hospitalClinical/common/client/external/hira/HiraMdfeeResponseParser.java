package com.example.hospitalClinical.common.client.external.hira;

import com.example.hospitalClinical.documentation.dto.HiraProcedureItemDto;
import com.example.hospitalClinical.documentation.dto.HiraProcedureSearchResult;
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
public class HiraMdfeeResponseParser {

    private final ObjectMapper objectMapper;

    public HiraProcedureSearchResult parse(String body, int requestedPageNo, int requestedNumOfRows) {
        if (body == null || body.isBlank()) {
            return emptyResult(requestedPageNo, requestedNumOfRows, "", "");
        }
        String lead = body.stripLeading();
        if (lead.startsWith("<")) {
            return parseXml(body, requestedPageNo, requestedNumOfRows);
        }
        return parseJson(body, requestedPageNo, requestedNumOfRows);
    }

    private HiraProcedureSearchResult parseJson(String json, int requestedPageNo, int requestedNumOfRows) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode envelope = root.has("response") ? root.get("response") : root;
            JsonNode header = envelope.path("header");
            String resultCode = text(header, "resultCode");
            String resultMsg = text(header, "resultMsg");
            if (!"00".equals(resultCode) && !"0".equals(resultCode)) {
                return emptyResult(requestedPageNo, requestedNumOfRows, resultCode, resultMsg);
            }
            JsonNode bodyNode = envelope.path("body");
            int total = parseInt(bodyNode.path("totalCount"), 0);
            int pageNo = parseInt(bodyNode.path("pageNo"), requestedPageNo);
            int numOfRows = parseInt(bodyNode.path("numOfRows"), requestedNumOfRows);
            List<HiraProcedureItemDto> items = collectItemsFromJsonBody(bodyNode);
            return HiraProcedureSearchResult.builder()
                    .resultCode(resultCode)
                    .resultMsg(resultMsg)
                    .pageNo(pageNo)
                    .numOfRows(numOfRows)
                    .totalCount(total)
                    .items(items)
                    .build();
        } catch (Exception e) {
            return emptyResult(requestedPageNo, requestedNumOfRows, "PARSE", e.getMessage());
        }
    }

    private HiraProcedureSearchResult parseXml(String xml, int requestedPageNo, int requestedNumOfRows) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));
            String resultCode = firstTagText(doc, "resultCode");
            String resultMsg = firstTagText(doc, "resultMsg");
            if (resultCode != null && !resultCode.isBlank() && !"00".equals(resultCode) && !"0".equals(resultCode)) {
                return emptyResult(requestedPageNo, requestedNumOfRows, resultCode, resultMsg);
            }
            int total = parseIntText(firstTagText(doc, "totalCount"), 0);
            int pageNo = parseIntText(firstTagText(doc, "pageNo"), requestedPageNo);
            int numOfRows = parseIntText(firstTagText(doc, "numOfRows"), requestedNumOfRows);
            NodeList itemNodes = doc.getElementsByTagNameNS("*", "item");
            if (itemNodes.getLength() == 0) {
                itemNodes = doc.getElementsByTagName("item");
            }
            List<HiraProcedureItemDto> items = new ArrayList<>();
            for (int i = 0; i < itemNodes.getLength(); i++) {
                if (!(itemNodes.item(i) instanceof Element el)) {
                    continue;
                }
                String cd = firstNonBlank(
                        elementText(el, "mdfeeCd"),
                        elementText(el, "diagActCd"),
                        elementText(el, "actCd"));
                String nm = firstNonBlank(
                        elementText(el, "korNm"),
                        elementText(el, "mdfeeKorNm"),
                        elementText(el, "actKorNm"));
                String div = elementText(el, "mdfeeDivNo");
                if (!cd.isEmpty() || !nm.isEmpty()) {
                    items.add(HiraProcedureItemDto.builder()
                            .mdfeeCd(cd.isEmpty() ? null : cd)
                            .korNm(nm.isEmpty() ? null : nm)
                            .mdfeeDivNo(div.isEmpty() ? null : div)
                            .build());
                }
            }
            return HiraProcedureSearchResult.builder()
                    .resultCode(resultCode != null ? resultCode : "00")
                    .resultMsg(resultMsg)
                    .pageNo(pageNo)
                    .numOfRows(numOfRows)
                    .totalCount(total)
                    .items(items)
                    .build();
        } catch (Exception e) {
            return emptyResult(requestedPageNo, requestedNumOfRows, "PARSE", e.getMessage());
        }
    }

    private static HiraProcedureSearchResult emptyResult(int pageNo, int numOfRows, String code, String msg) {
        return HiraProcedureSearchResult.builder()
                .resultCode(code)
                .resultMsg(msg)
                .pageNo(pageNo)
                .numOfRows(numOfRows)
                .totalCount(0)
                .items(List.of())
                .build();
    }

    private List<HiraProcedureItemDto> collectItemsFromJsonBody(JsonNode bodyNode) {
        List<HiraProcedureItemDto> fromItems = mapItems(bodyNode.path("items"));
        if (!fromItems.isEmpty()) {
            return fromItems;
        }
        JsonNode directItem = bodyNode.path("item");
        if (directItem.isArray()) {
            List<HiraProcedureItemDto> out = new ArrayList<>();
            for (JsonNode n : directItem) {
                addItem(out, n);
            }
            return out;
        }
        if (directItem.isObject() && !directItem.isMissingNode()) {
            List<HiraProcedureItemDto> out = new ArrayList<>();
            addItem(out, directItem);
            return out;
        }
        return fromItems;
    }

    private List<HiraProcedureItemDto> mapItems(JsonNode itemsNode) {
        List<HiraProcedureItemDto> out = new ArrayList<>();
        if (itemsNode == null || itemsNode.isMissingNode() || itemsNode.isNull()) {
            return out;
        }
        if (itemsNode.isArray()) {
            for (JsonNode n : itemsNode) {
                addItem(out, n);
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
                    addItem(out, n);
                }
            } else {
                addItem(out, item);
            }
        }
        return out;
    }

    private static void addItem(List<HiraProcedureItemDto> out, JsonNode n) {
        if (n == null || n.isNull() || n.isMissingNode()) {
            return;
        }
        String cd = firstNonBlank(
                text(n, "mdfeeCd"),
                text(n, "diagActCd"),
                text(n, "actCd"));
        String nm = firstNonBlank(
                text(n, "korNm"),
                text(n, "mdfeeKorNm"),
                text(n, "actKorNm"));
        String div = text(n, "mdfeeDivNo");
        if (!cd.isEmpty() || !nm.isEmpty()) {
            out.add(HiraProcedureItemDto.builder()
                    .mdfeeCd(cd.isEmpty() ? null : cd)
                    .korNm(nm.isEmpty() ? null : nm)
                    .mdfeeDivNo(div.isEmpty() ? null : div)
                    .build());
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.path(field);
        return v.isMissingNode() || v.isNull() ? "" : v.asText("").trim();
    }

    private static int parseInt(JsonNode n, int dflt) {
        if (n == null || n.isMissingNode() || n.isNull()) {
            return dflt;
        }
        try {
            return Integer.parseInt(n.asText().trim());
        } catch (NumberFormatException e) {
            return dflt;
        }
    }

    private static int parseIntText(String s, int dflt) {
        if (s == null || s.isBlank()) {
            return dflt;
        }
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return dflt;
        }
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

    private static String firstTagText(Document doc, String tag) {
        NodeList nl = doc.getElementsByTagNameNS("*", tag);
        if (nl.getLength() == 0) {
            nl = doc.getElementsByTagName(tag);
        }
        if (nl.getLength() == 0) {
            return "";
        }
        return nl.item(0).getTextContent() != null ? nl.item(0).getTextContent().trim() : "";
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
}
