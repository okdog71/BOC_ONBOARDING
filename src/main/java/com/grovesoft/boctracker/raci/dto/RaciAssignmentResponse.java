package com.grovesoft.boctracker.raci.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "RACI 배정 응답")
public record RaciAssignmentResponse(
        @Schema(description = "ID", example = "1") Long id,
        @Schema(description = "도메인 그룹", example = "redeem/order") String domainGroup,
        @Schema(description = "범위", example = "쿠폰·주문 도메인") String scope,
        @Schema(description = "복잡도(상/중/하)", example = "상") String complexity,
        @Schema(description = "규모(상/중/하)", example = "중") String scale,
        @Schema(description = "세대(A/B/혼용/미확인/-)", example = "B") String generation,
        @Schema(description = "담당(R)", example = "담당자 A") String responsible,
        @Schema(description = "백업", example = "담당자 B") String backup,
        @Schema(description = "리뷰어", example = "리드") String reviewer,
        @Schema(description = "메뉴 수", example = "12") Integer menuCount,
        @Schema(description = "상태(미착수/진행중/완료/보류)", example = "미착수") String status,
        @Schema(description = "목표일", example = "2026-12-15") LocalDate targetDate,
        @Schema(description = "비고") String note,
        @Schema(description = "생성 시각", example = "2026-11-02T09:30:00") LocalDateTime createdAt,
        @Schema(description = "수정 시각", example = "2026-11-02T09:30:00") LocalDateTime updatedAt,
        @Schema(description = "등록자(로그인 ID)", example = "level9") String createdBy,
        @Schema(description = "수정자(로그인 ID)", example = "level9") String updatedBy) {
}
