package com.grovesoft.boctracker.review.dto;

import com.grovesoft.boctracker.common.validation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "회고 리뷰 로그 생성/수정 요청")
public record ReviewLogRequest(
        @Schema(description = "회차", example = "1") @NotNull @Min(1) Integer round,
        @Schema(description = "리뷰일", example = "2026-11-10") LocalDate reviewDate,
        @Schema(description = "대상 도메인", example = "redeem") @Size(max = 255) String targetDomain,
        @Schema(description = "발표자", example = "담당자 A") @Size(max = 255) String presenter,
        @Schema(description = "참석자", example = "리드, 담당자 B") @Size(max = 1000) String attendees,
        @Schema(description = "커버리지(%)", example = "80") @Min(0) @Max(100) Integer coveragePct,
        @Schema(description = "교차 검증", example = "스토리보드 대조") @Size(max = 1000) String crossCheck,
        @Schema(description = "갭", example = "예외 케이스 누락") @Size(max = 1000) String gaps,
        @Schema(description = "dev 미확인", example = "mock 응답 확인 필요") @Size(max = 1000) String devUnconfirmed,
        @Schema(description = "조치", example = "보완 분석") @Size(max = 1000) String action,
        @Schema(description = "조치 담당", example = "담당자 A") @Size(max = 100) String actionOwner,
        @Schema(description = "목표일", example = "2026-11-17") LocalDate targetDate,
        @Schema(description = "상태(미착수/진행중/완료/보류)", example = "미착수") @EnumValue(allowed = {"미착수", "진행중", "완료", "보류"}) @Size(max = 20) String status) {
}
