package com.example.hospitalClinical.common.client.external.hira;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class HiraApiClient {

    private static final String DIAGNOSS_MDFEE_PATH = "/B551182/mdfeeCrtrInfoService/getDiagnossMdfeeList";

    private final HiraApiProperties properties;
    private final RestClient restClient = RestClient.create();

    public String fetchDiagnosisMdfeeList(int pageNo, int numOfRows, String korNm) {
        String base = properties.getBaseUrl();
        String key = properties.getServiceKey();
        if (base == null || base.isBlank() || key == null || key.isBlank() || "인증키".equals(key.trim())) {
            throw new IllegalStateException(
                    "hira.api.service-key 가 비어 있습니다. 환경변수 HIRA_API_KEY 를 설정하세요.");
        }
        String root = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        UriComponentsBuilder b = UriComponentsBuilder
                .fromUriString(root + DIAGNOSS_MDFEE_PATH)
                .queryParam("serviceKey", key)
                .queryParam("pageNo", String.valueOf(pageNo))
                .queryParam("numOfRows", String.valueOf(numOfRows))
                .queryParam("type", "json");
        if (StringUtils.hasText(korNm)) {
            b.queryParam("korNm", korNm.trim());
        }
        URI uri = b.encode(StandardCharsets.UTF_8).build().toUri();
        byte[] raw = restClient.get()
                .uri(uri)
                .retrieve()
                .body(byte[].class);
        if (raw == null || raw.length == 0) {
            return "";
        }
        return new String(raw, StandardCharsets.UTF_8);
    }
}
