package com.example.hospitalClinical.common.client.external.clinicalsupport;

import com.example.hospitalClinical.common.response.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
@Slf4j
public class NursingSupportOrderApiClient {

    private static final String MEDICATION_PATH = "/api/medicationRecord";
    private static final String TREATMENT_PATH = "/api/treatmentResult";

    private static final ParameterizedTypeReference<ApiResponse<JsonNode>> ENVELOPE =
            new ParameterizedTypeReference<>() {};

    private final ClinicalSupportApiProperties properties;
    private final ObjectMapper objectMapper;
    private RestClient restClient;

    @PostConstruct
    void init() {
        if (!properties.isEnabled()) {
            return;
        }
        String base = properties.getBaseUrl();
        if (base == null || base.isBlank()) {
            return;
        }
        String root = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        this.restClient = RestClient.builder().baseUrl(root).build();
    }

    public void postMedicationRecord(MedicationRecordOutboundRequest body) {
        if (body == null) {
            return;
        }
        try {
            postHttp(MEDICATION_PATH, objectMapper.writeValueAsString(body), body.getMedicationId());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("진료지원 요청 JSON 직렬화 실패 path=" + MEDICATION_PATH, e);
        }
    }

    public void postTreatmentResult(TreatmentResultOutboundRequest body) {
        if (body == null) {
            return;
        }
        try {
            postHttp(
                    TREATMENT_PATH,
                    objectMapper.writeValueAsString(body),
                    body.getProcedureResultId());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("진료지원 요청 JSON 직렬화 실패 path=" + TREATMENT_PATH, e);
        }
    }

    private void postHttp(String path, String json, String idForLog) {
        if (!properties.isEnabled()) {
            log.info("진료지원 API 비활성화됨 — {} 동기화 생략 id={}", path, idForLog);
            return;
        }
        if (restClient == null) {
            throw new IllegalStateException("clinical-support.api.base-url 이 비어 있습니다.");
        }
        try {
            log.debug("진료지원 POST {} body={}", path, json);
            ApiResponse<JsonNode> envelope = restClient.post()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
                    .retrieve()
                    .body(ENVELOPE);
            if (envelope == null) {
                throw new IllegalStateException("진료지원 API 응답이 비어 있습니다.");
            }
            if (!envelope.isSuccess()) {
                throw new IllegalStateException(
                        "진료지원 API 실패: " + (envelope.getMessage() != null ? envelope.getMessage() : ""));
            }
        } catch (RestClientException e) {
            log.warn("진료지원 API 호출 실패 path={} id={}", path, idForLog, e);
            throw e;
        }
    }
}
