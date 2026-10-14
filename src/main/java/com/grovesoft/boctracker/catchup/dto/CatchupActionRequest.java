package com.grovesoft.boctracker.catchup.dto;

import com.grovesoft.boctracker.common.validation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "캐치업 액션 생성/수정 요청")
public record CatchupActionRequest(
        @Schema(description = "구분", example = "투입 전 활용") @NotBlank @Size(max = 100) String category,
        @Schema(description = "액션", example = "스토리보드 사전 리뷰") @NotBlank @Size(max = 1000) String action,
        @Schema(description = "목적", example = "도메인 이해도 선확보") @Size(max = 1000) String purpose,
        @Schema(description = "시기(투입전/투입후/상시)", example = "투입전") @EnumValue(allowed = {"투입전", "투입후", "상시"}) @Size(max = 20) String timing,
        @Schema(description = "담당", example = "BOC 리드") @Size(max = 100) String owner,
        @Schema(description = "KPI", example = "리뷰 완료율 100%") @Size(max = 1000) String kpi,
        @Schema(description = "상태(미착수/진행중/완료/보류)", example = "미착수") @EnumValue(allowed = {"미착수", "진행중", "완료", "보류"}) @Size(max = 20) String status,
        @Schema(description = "비고") @Size(max = 1000) String note) {
}
