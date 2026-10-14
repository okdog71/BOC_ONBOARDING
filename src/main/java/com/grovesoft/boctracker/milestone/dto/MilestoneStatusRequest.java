package com.grovesoft.boctracker.milestone.dto;

import com.grovesoft.boctracker.common.validation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "마일스톤 상태 변경 요청")
public record MilestoneStatusRequest(
        @Schema(description = "상태", example = "진행중")
        @EnumValue(allowed = {"미착수", "진행중", "완료", "보류"}, nullable = false) String status) {
}
