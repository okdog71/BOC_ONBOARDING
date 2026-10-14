package com.grovesoft.boctracker.common.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공통 응답 래퍼. 성공 시 data, 실패 시 error 가 채워진다.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        @Schema(description = "성공 여부", example = "true") boolean success,
        @Schema(description = "응답 데이터(실패 시 없음)") T data,
        @Schema(description = "오류 정보(성공 시 없음)") ErrorResponse error) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> fail(ErrorResponse error) {
        return new ApiResponse<>(false, null, error);
    }
}
