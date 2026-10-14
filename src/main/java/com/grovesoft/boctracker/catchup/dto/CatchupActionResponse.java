package com.grovesoft.boctracker.catchup.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "캐치업 액션 응답")
public record CatchupActionResponse(
        @Schema(description = "ID", example = "1") Long id,
        @Schema(description = "구분", example = "투입 전 활용") String category,
        @Schema(description = "액션", example = "스토리보드 사전 리뷰") String action,
        @Schema(description = "목적", example = "도메인 이해도 선확보") String purpose,
        @Schema(description = "시기(투입전/투입후/상시)", example = "투입전") String timing,
        @Schema(description = "담당", example = "BOC 리드") String owner,
        @Schema(description = "KPI", example = "리뷰 완료율 100%") String kpi,
        @Schema(description = "상태(미착수/진행중/완료/보류)", example = "미착수") String status,
        @Schema(description = "비고") String note,
        @Schema(description = "생성 시각", example = "2026-11-02T09:30:00") LocalDateTime createdAt,
        @Schema(description = "수정 시각", example = "2026-11-02T09:30:00") LocalDateTime updatedAt) {
}
