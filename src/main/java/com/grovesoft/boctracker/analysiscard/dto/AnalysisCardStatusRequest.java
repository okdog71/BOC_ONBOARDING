package com.grovesoft.boctracker.analysiscard.dto;

import com.grovesoft.boctracker.common.validation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메뉴별 분석 카드 상태 변경 요청")
public record AnalysisCardStatusRequest(
        @Schema(description = "상태", example = "진행중") @EnumValue(allowed = {"미착수", "진행중", "완료", "보류"}, nullable = false) String status) {
}
