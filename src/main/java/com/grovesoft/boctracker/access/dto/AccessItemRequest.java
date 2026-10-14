package com.grovesoft.boctracker.access.dto;

import com.grovesoft.boctracker.common.validation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "접근권한 생성/수정 요청")
public record AccessItemRequest(
        @Schema(description = "구분", example = "저장소") @NotBlank @Size(max = 100) String category,
        @Schema(description = "대상", example = "Git 저장소") @NotBlank @Size(max = 255) String target,
        @Schema(description = "용도", example = "소스 열람") @Size(max = 1000) String usage,
        @Schema(description = "환경 범위", example = "dev") @Size(max = 255) String envScope,
        @Schema(description = "신청 절차", example = "사내 포털 신청") @Size(max = 1000) String procedure,
        @Schema(description = "상태(미신청/신청/승인/거부/보류)", example = "미신청") @EnumValue(allowed = {"미신청", "신청", "승인", "거부", "보류"}) @Size(max = 20) String status,
        @Schema(description = "신청일", example = "2026-11-02") LocalDate requestedDate,
        @Schema(description = "승인일") LocalDate approvedDate,
        @Schema(description = "담당", example = "인프라 담당") @Size(max = 100) String owner,
        @Schema(description = "비고") @Size(max = 1000) String note) {
}
