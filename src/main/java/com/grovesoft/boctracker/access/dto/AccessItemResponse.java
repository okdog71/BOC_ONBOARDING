package com.grovesoft.boctracker.access.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "접근권한 응답")
public record AccessItemResponse(
        @Schema(description = "ID", example = "1") Long id,
        @Schema(description = "구분", example = "저장소") String category,
        @Schema(description = "대상", example = "Git 저장소") String target,
        @Schema(description = "용도", example = "소스 열람") String usage,
        @Schema(description = "환경 범위", example = "dev") String envScope,
        @Schema(description = "신청 절차", example = "사내 포털 신청") String procedure,
        @Schema(description = "상태(미신청/신청/승인/거부/보류)", example = "미신청") String status,
        @Schema(description = "신청일", example = "2026-11-02") LocalDate requestedDate,
        @Schema(description = "승인일") LocalDate approvedDate,
        @Schema(description = "담당", example = "인프라 담당") String owner,
        @Schema(description = "비고") String note,
        @Schema(description = "생성 시각", example = "2026-11-02T09:30:00") LocalDateTime createdAt,
        @Schema(description = "수정 시각", example = "2026-11-02T09:30:00") LocalDateTime updatedAt) {
}
