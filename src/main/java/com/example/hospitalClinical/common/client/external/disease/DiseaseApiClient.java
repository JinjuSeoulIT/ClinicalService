package com.example.hospitalClinical.common.client.external.disease;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class DiseaseApiClient {

    private static final String PATH = "/B551182/diseaseInfoService1/getDissNameCodeList1";

    private final DiseaseApiProperties properties;
    private final RestClient restClient = RestClient.create();

    public String fetchDissNameCodeList(int pageNo, int numOfRows, String diseaseType, String searchText) {
        String base = properties.getBaseUrl();
        String key = properties.getServiceKey();
        if (base == null || base.isBlank() || key == null || key.isBlank() || "인증키".equals(key.trim())) {
            throw new IllegalStateException(
                    "disease.api.service-key 가 비어 있습니다. 환경변수 DISEASE_API_KEY 를 설정하세요.");
        }
        String root = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        UriComponentsBuilder b = UriComponentsBuilder
                .fromUriString(root + PATH)
                .queryParam("serviceKey", key)
                .queryParam("numOfRows", String.valueOf(numOfRows))
                .queryParam("pageNo", String.valueOf(pageNo))
                .queryParam("sickType", "1")
                .queryParam("medTp", "1")
                .queryParam("diseaseType", diseaseType);
        if (StringUtils.hasText(searchText)) {
            b.queryParam("searchText", searchText.trim());
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
