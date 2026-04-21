package com.example.hospitalClinical.common.client.external.clinicalsupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ClinicalSupportTestExecutionClient {

    private final RestTemplate restTemplate;
    private final ClinicalSupportApiProperties properties;
    private final ObjectMapper objectMapper;

    private volatile List<ClinicalSupportTestExecutionRow> cache;
    private volatile long cacheExpiresAtMillis;

    public List<ClinicalSupportTestExecutionRow> fetchAllExecutions() {
        if (!properties.isEnabled()) {
            return List.of();
        }
        String base = properties.getBaseUrl() == null ? "" : properties.getBaseUrl().trim();
        if (base.isEmpty()) {
            return List.of();
        }
        int ttl = Math.max(0, properties.getExecutionListCacheTtlMs());
        long now = System.currentTimeMillis();
        if (ttl > 0 && cache != null && now < cacheExpiresAtMillis) {
            return cache;
        }
        synchronized (this) {
            if (ttl > 0 && cache != null && now < cacheExpiresAtMillis) {
                return cache;
            }
            List<ClinicalSupportTestExecutionRow> fresh = fetchRemoteAndParse(base);
            if (ttl > 0 && !fresh.isEmpty()) {
                cache = fresh;
                cacheExpiresAtMillis = now + ttl;
            } else {
                cache = null;
                cacheExpiresAtMillis = 0L;
            }
            return fresh;
        }
    }

    private List<ClinicalSupportTestExecutionRow> fetchRemoteAndParse(String base) {
        try {
            String normalized = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
            String url =
                    UriComponentsBuilder.fromUriString(normalized).path("/api/testExecution").build(true).toUriString();
            String raw = restTemplate.getForObject(url, String.class);
            if (raw == null || raw.isBlank()) {
                return List.of();
            }
            JsonNode root = objectMapper.readTree(raw);
            JsonNode arr = root.get("result");
            if (arr == null || !arr.isArray()) {
                arr = root.get("data");
            }
            if (arr == null || !arr.isArray()) {
                return List.of();
            }
            List<ClinicalSupportTestExecutionRow> out = new ArrayList<>();
            for (JsonNode el : arr) {
                if (el == null || !el.isObject()) {
                    continue;
                }
                Long orderItemId =
                        firstLong(
                                el,
                                "orderItemId",
                                "order_item_id",
                                "ORDER_ITEM_ID",
                                "orderitemid");
                String progress =
                        firstText(
                                el,
                                "progressStatus",
                                "progress_status",
                                "PROGRESS_STATUS",
                                "status",
                                "STATUS");
                if (orderItemId == null) {
                    continue;
                }
                ClinicalSupportTestExecutionRow row = new ClinicalSupportTestExecutionRow();
                row.setOrderItemId(orderItemId);
                row.setProgressStatus(progress);
                out.add(row);
            }
            return out;
        } catch (Exception e) {
            return List.of();
        }
    }

    private static Long firstLong(JsonNode el, String... fieldNames) {
        for (String name : fieldNames) {
            JsonNode v = el.get(name);
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

    private static String firstText(JsonNode el, String... fieldNames) {
        for (String name : fieldNames) {
            JsonNode v = el.get(name);
            if (v == null || v.isNull()) {
                continue;
            }
            if (v.isTextual()) {
                String t = v.asText().trim();
                if (!t.isEmpty()) {
                    return t;
                }
            }
            if (v.isIntegralNumber()) {
                return String.valueOf(v.longValue());
            }
        }
        return null;
    }
}
