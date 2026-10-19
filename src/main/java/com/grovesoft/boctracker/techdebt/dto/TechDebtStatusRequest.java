package com.grovesoft.boctracker.techdebt.dto;

import com.grovesoft.boctracker.common.validation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "기술부채 상태 변경 요청")
public record TechDebtStatusRequest(
        @Schema(description = "상태", example = "확인중")
        @EnumValue(allowed = {"미착수", "확인중", "진행중", "검증중", "완료", "보류"}, nullable = false) String status) {
}
