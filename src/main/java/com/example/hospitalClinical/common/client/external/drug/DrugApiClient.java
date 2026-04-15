package com.example.hospitalClinical.common.client.external.drug;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class DrugApiClient {

    private final DrugApiProperties properties;
    private final RestClient restClient = RestClient.create();

    public String fetchEasyDrugList(int pageNo, int numOfRows, String itemName, String itemSeq) {
        String base = properties.getBaseUrl();
        String key = properties.getServiceKey();
        if (base == null || base.isBlank() || key == null || key.isBlank() || "인증키".equals(key.trim())) {
            throw new IllegalStateException(
                    "drug.api.service-key 가 비어 있습니다. application.yml 의 ${DRUG_API_KEY} 에 해당하는 환경변수를 설정하세요.");
        }
        String root = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        UriComponentsBuilder b = UriComponentsBuilder
                .fromUriString(root + "/1471000/DrbEasyDrugInfoService/getDrbEasyDrugList")
                .queryParam("serviceKey", key)
                .queryParam("pageNo", String.valueOf(pageNo))
                .queryParam("numOfRows", String.valueOf(numOfRows))
                .queryParam("type", "json");
        if (StringUtils.hasText(itemSeq)) {
            b.queryParam("itemSeq", itemSeq.trim());
        } else if (StringUtils.hasText(itemName)) {
            b.queryParam("itemName", itemName.trim());
        }
        URI uri = b.encode(StandardCharsets.UTF_8).build().toUri();
        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(String.class);
    }
}
