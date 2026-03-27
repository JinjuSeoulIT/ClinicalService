package com.example.hospitalClinical.common.client.external.clinicalsupport;

import com.example.hospitalClinical.common.response.ApiResponse;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TestExecutionApiClient {

    private static final String PATH = "/api/testExecution";

    private static final ParameterizedTypeReference<ApiResponse<JsonNode>> ENVELOPE =
            new ParameterizedTypeReference<>() {
            };

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

    public List<TestExecutionRemoteRow> register(TestExecutionRegisterRequest request) {
        if (!properties.isEnabled()) {
            return Collections.emptyList();
        }
        if (restClient == null) {
            throw new IllegalStateException("clinical-support.api.base-url 이 비어 있습니다.");
        }
        try {
            ApiResponse<JsonNode> envelope = restClient.post()
                    .uri(PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(ENVELOPE);
            if (envelope == null) {
                throw new IllegalStateException("진료지원 검사수행 API 응답이 비어 있습니다.");
            }
            if (!envelope.isSuccess()) {
                throw new IllegalStateException(
                        "진료지원 검사수행 API 실패: " + (envelope.getMessage() != null ? envelope.getMessage() : ""));
            }
            return rowsFromResultNode(envelope.getResult());
        } catch (RestClientException e) {
            log.warn("진료지원 검사수행 API 호출 실패 orderItemId={}", request.getOrderItemId(), e);
            throw e;
        }
    }

    private List<TestExecutionRemoteRow> rowsFromResultNode(JsonNode resultNode) {
        if (resultNode == null || resultNode.isNull()) {
            return Collections.emptyList();
        }
        if (resultNode.isArray()) {
            List<TestExecutionRemoteRow> out = new ArrayList<>(resultNode.size());
            for (JsonNode n : resultNode) {
                out.add(objectMapper.convertValue(n, TestExecutionRemoteRow.class));
            }
            return out;
        }
        if (resultNode.isObject()) {
            return List.of(objectMapper.convertValue(resultNode, TestExecutionRemoteRow.class));
        }
        return Collections.emptyList();
    }
}
