package com.example.hospitalClinical.common.client.internal.reception;

import com.example.hospitalClinical.common.exception.BusinessException;
import com.example.hospitalClinical.common.exception.ErrorCode;
import com.example.hospitalClinical.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;

@Component
public class ReceptionClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ReceptionClient(RestTemplate restTemplate,
                           @Value("${reception.api.base-url:http://192.168.1.70:8283}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    }

    public ReceptionResponse getReception(Long receptionId) {
        String url = baseUrl + "api/receptions/" + receptionId;
        try {
            ResponseEntity<ApiResponse<ReceptionResponse>> res = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<ReceptionResponse>>() {}
            );
            ApiResponse<ReceptionResponse> body = res.getBody();
            if (body == null || !body.isSuccess()) {
                throw new BusinessException(ErrorCode.RECEPTION_API_ERROR,
                        body != null && body.getMessage() != null ? body.getMessage() : "접수 조회 실패");
            }
            ReceptionResponse result = body.getResult();
            if (result == null) {
                throw new BusinessException(ErrorCode.RECEPTION_NOT_FOUND);
            }
            return result;
        } catch (HttpClientErrorException.NotFound e) {
            throw new BusinessException(ErrorCode.RECEPTION_NOT_FOUND);
        } catch (HttpClientErrorException e) {
            throw new BusinessException(ErrorCode.RECEPTION_API_ERROR,
                    "접수 서비스 오류: " + (e.getResponseBodyAsString() != null ? e.getResponseBodyAsString() : e.getStatusCode().toString()));
        } catch (HttpServerErrorException e) {
            throw new BusinessException(ErrorCode.RECEPTION_API_ERROR,
                    "접수 서비스 오류: " + e.getStatusCode());
        }
    }

    public ReceptionResponse updateReceptionStatus(Long receptionId, ReceptionStatusUpdateRequest request) {
        String url = baseUrl + "api/receptions/" + receptionId + "/status";
        HttpEntity<ReceptionStatusUpdateRequest> entity = new HttpEntity<>(request);
        try {
            ResponseEntity<ApiResponse<ReceptionResponse>> res = restTemplate.exchange(
                    url,
                    HttpMethod.PATCH,
                    entity,
                    new ParameterizedTypeReference<ApiResponse<ReceptionResponse>>() {}
            );
            ApiResponse<ReceptionResponse> body = res.getBody();
            if (body == null || !body.isSuccess()) {
                throw new BusinessException(ErrorCode.RECEPTION_API_ERROR,
                        body != null && body.getMessage() != null ? body.getMessage() : "접수 상태 변경 실패");
            }
            return body.getResult() != null ? body.getResult() : new ReceptionResponse();
        } catch (HttpClientErrorException.NotFound e) {
            throw new BusinessException(ErrorCode.RECEPTION_NOT_FOUND);
        } catch (HttpClientErrorException e) {
            throw new BusinessException(ErrorCode.RECEPTION_API_ERROR,
                    "접수 상태 변경 실패: " + (e.getResponseBodyAsString() != null ? e.getResponseBodyAsString() : e.getStatusCode().toString()));
        } catch (HttpServerErrorException e) {
            throw new BusinessException(ErrorCode.RECEPTION_API_ERROR,
                    "접수 상태 변경 실패: " + e.getStatusCode());
        }
    }

    public List<ReceptionResponse> getReceptionQueue(Long departmentId, String doctorId, String date) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "api/receptions")
                .queryParamIfPresent("departmentId", departmentId != null ? java.util.Optional.of(departmentId) : java.util.Optional.empty())
                .queryParamIfPresent(
                        "doctorId",
                        doctorId != null && !doctorId.isBlank() ? java.util.Optional.of(doctorId.trim()) : java.util.Optional.empty())
                .queryParamIfPresent("dateFrom", date != null && !date.isBlank() ? java.util.Optional.of(date.trim()) : java.util.Optional.empty())
                .queryParamIfPresent("dateTo", date != null && !date.isBlank() ? java.util.Optional.of(date.trim()) : java.util.Optional.empty())
                .toUriString();
        try {
            ResponseEntity<ApiResponse<List<ReceptionResponse>>> res = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<List<ReceptionResponse>>>() {}
            );
            ApiResponse<List<ReceptionResponse>> body = res.getBody();
            if (body == null || !body.isSuccess()) {
                throw new BusinessException(ErrorCode.RECEPTION_API_ERROR,
                        body != null && body.getMessage() != null ? body.getMessage() : "접수 대기열 조회 실패");
            }
            List<ReceptionResponse> result = body.getResult();
            return result != null ? result : Collections.emptyList();
        } catch (HttpClientErrorException e) {
            throw new BusinessException(ErrorCode.RECEPTION_API_ERROR,
                    "접수 대기열 조회 실패: " + (e.getResponseBodyAsString() != null ? e.getResponseBodyAsString() : e.getStatusCode()));
        } catch (HttpServerErrorException e) {
            throw new BusinessException(ErrorCode.RECEPTION_API_ERROR,
                    "접수 대기열 조회 실패: " + e.getStatusCode());
        }
    }
}
