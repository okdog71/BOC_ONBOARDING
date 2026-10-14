package com.grovesoft.boctracker.milestone.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "마일스톤 응답")
public record MilestoneResponse(
        @Schema(description = "ID", example = "1") Long id,
        @Schema(description = "단계", example = "P0. 수급·준비") String phase,
        @Schema(description = "마일스톤 코드(유일)", example = "M0.1") String code,
        @Schema(description = "마일스톤명", example = "수급 산출물 확인") String name,
        @Schema(description = "목표", example = "기존 BOC 운영 산출물의 존재/위치/최신성/owner 확인") String goal,
        @Schema(description = "체크포인트", example = "스토리보드·업무흐름도·IA·Jira·Confluence·Git 존재 여부") String checkpoint,
        @Schema(description = "산출물", example = "수급 산출물 확인표(②)") String output,
        @Schema(description = "선행 조건", example = "현업 인터뷰·접근 승인") String precondition,
        @Schema(description = "담당", example = "BOC 리드") String owner,
        @Schema(description = "소요 기간(주)", example = "2") String durationWeeks,
        @Schema(description = "상태(미착수/진행중/완료/보류)", example = "미착수") String status,
        @Schema(description = "완료 기준", example = "수급 목록 확정·신뢰도 등급 부여") String doneCriteria,
        @Schema(description = "비고", example = "투입 전 착수 권장") String note,
        @Schema(description = "생성 시각", example = "2026-11-02T09:30:00") LocalDateTime createdAt,
        @Schema(description = "수정 시각", example = "2026-11-02T09:30:00") LocalDateTime updatedAt) {
}
