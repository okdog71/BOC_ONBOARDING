package com.grovesoft.boctracker.supplydoc.dto;

import com.grovesoft.boctracker.common.validation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "수급 산출물 상태 변경 요청")
public record SupplyDocStatusRequest(
        @Schema(description = "상태", example = "요청") @EnumValue(allowed = {"미요청", "요청", "수령", "해당없음"}, nullable = false) String status) {
}
