package com.grovesoft.boctracker.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "회고 리뷰 로그 응답")
public record ReviewLogResponse(
        @Schema(description = "ID", example = "1") Long id,
        @Schema(description = "회차", example = "1") Integer round,
        @Schema(description = "리뷰일", example = "2026-11-10") LocalDate reviewDate,
        @Schema(description = "대상 도메인", example = "redeem") String targetDomain,
        @Schema(description = "발표자", example = "담당자 A") String presenter,
        @Schema(description = "참석자", example = "리드, 담당자 B") String attendees,
        @Schema(description = "커버리지(%)", example = "80") Integer coveragePct,
        @Schema(description = "교차 검증", example = "스토리보드 대조") String crossCheck,
        @Schema(description = "갭", example = "예외 케이스 누락") String gaps,
        @Schema(description = "dev 미확인", example = "mock 응답 확인 필요") String devUnconfirmed,
        @Schema(description = "조치", example = "보완 분석") String action,
        @Schema(description = "조치 담당", example = "담당자 A") String actionOwner,
        @Schema(description = "목표일", example = "2026-11-17") LocalDate targetDate,
        @Schema(description = "상태(미착수/진행중/완료/보류)", example = "미착수") String status,
        @Schema(description = "생성 시각", example = "2026-11-02T09:30:00") LocalDateTime createdAt,
        @Schema(description = "수정 시각", example = "2026-11-02T09:30:00") LocalDateTime updatedAt,
        @Schema(description = "등록자(로그인 ID)", example = "level9") String createdBy,
        @Schema(description = "수정자(로그인 ID)", example = "level9") String updatedBy) {
}
