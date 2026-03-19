package com.example.hospitalClinical.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message = "";
    private T result;

    public static <T> ApiResponse<T> ok(T result) {
        return new ApiResponse<>(true, "", result);
    }

    public static <T> ApiResponse<T> ok(String message, T result) {
        return new ApiResponse<>(true, message, result);
    }

    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, message, null);
    }

    public static <T> ApiResponse<T> fail(String message, T result) {
        return new ApiResponse<>(false, message, result);
    }
}
