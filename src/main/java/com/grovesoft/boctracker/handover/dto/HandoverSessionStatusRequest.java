package com.grovesoft.boctracker.handover.dto;

import com.grovesoft.boctracker.common.validation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "인수인계 세션 상태 변경 요청")
public record HandoverSessionStatusRequest(
        @Schema(description = "상태", example = "예정")
        @EnumValue(allowed = {"미착수", "예정", "진행중", "완료", "보류"}, nullable = false) String status) {
}
