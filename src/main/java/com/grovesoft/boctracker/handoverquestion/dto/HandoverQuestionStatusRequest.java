package com.grovesoft.boctracker.handoverquestion.dto;

import com.grovesoft.boctracker.common.validation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "인수인계 질의 상태 변경 요청")
public record HandoverQuestionStatusRequest(
        @Schema(description = "상태", example = "진행중")
        @EnumValue(allowed = {"미확인", "진행중", "확인완료", "보류"}, nullable = false) String status) {
}
