package com.example.hospitalClinical.common.client.external.billing;

import com.example.hospitalClinical.common.response.ApiResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class BillingApiClient {

    private static final String PATH = "/api/billing/claims";
    private static final ParameterizedTypeReference<ApiResponse<BillingClinicalCompletedResult>> ENVELOPE =
            new ParameterizedTypeReference<>() {
            };

    private final BillingApiProperties properties;
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

    public ApiResponse<BillingClinicalCompletedResult> notifyClinicalCompleted(BillingClinicalCompletedRequest request) {
        if (!properties.isEnabled()) {
            return ApiResponse.ok("billing.api.enabled=false", null);
        }
        if (restClient == null) {
            throw new IllegalStateException("billing.api.base-url 이 비어 있습니다.");
        }
        ApiResponse<BillingClinicalCompletedResult> envelope = restClient.post()
                .uri(PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(ENVELOPE);
        if (envelope == null) {
            throw new IllegalStateException("수납 연동 API 응답이 비어 있습니다.");
        }
        return envelope;
    }
}
