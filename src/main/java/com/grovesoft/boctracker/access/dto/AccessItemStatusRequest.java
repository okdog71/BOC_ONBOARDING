package com.grovesoft.boctracker.access.dto;

import com.grovesoft.boctracker.common.validation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "접근권한 상태 변경 요청")
public record AccessItemStatusRequest(
        @Schema(description = "상태", example = "신청") @EnumValue(allowed = {"미신청", "신청", "승인", "거부", "보류"}, nullable = false) String status) {
}
