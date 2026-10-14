package com.grovesoft.boctracker.common.web;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "오류 정보")
public record ErrorResponse(
        @Schema(description = "오류 코드(VALIDATION_ERROR/BAD_REQUEST/NOT_FOUND/CONFLICT/INTERNAL_ERROR)",
                example = "VALIDATION_ERROR") String code,
        @Schema(description = "오류 메시지", example = "입력값 검증에 실패했습니다.") String message,
        @Schema(description = "필드별 검증 오류(검증 실패 시)") List<FieldError> fieldErrors) {

    @Schema(description = "필드 검증 오류")
    public record FieldError(
            @Schema(description = "필드명", example = "status") String field,
            @Schema(description = "오류 메시지", example = "허용되지 않은 값입니다. 허용값: 보류, 미착수, 완료, 진행중") String message) {
    }

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, List.of());
    }
}
