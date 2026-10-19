package com.grovesoft.boctracker.access.dto;

import com.grovesoft.boctracker.common.validation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "접근권한 생성/수정 요청. 계정 비밀번호·토큰 등 민감정보는 입력하지 않는다.")
public record AccessItemRequest(
        @Schema(description = "코드. 생성 시 생략하면 AC.{순번}으로 자동 부여. 부여 후 변경 불가", example = "AC.1")
        @Size(max = 50) String code,
        @Schema(description = "구분", example = "저장소") @NotBlank @Size(max = 100) String category,
        @Schema(description = "대상", example = "Git 저장소") @NotBlank @Size(max = 255) String target,
        @Schema(description = "용도", example = "소스 열람") @Size(max = 1000) String usage,
        @Schema(description = "환경 범위", example = "dev") @Size(max = 255) String envScope,
        @Schema(description = "신청 절차", example = "사내 포털 신청") @Size(max = 1000) String procedure,
        @Schema(description = "상태(미신청/신청/승인/거부/보류). 일자는 상태 전이 시 자동 보정", example = "미신청")
        @EnumValue(allowed = {"미신청", "신청", "승인", "거부", "보류"}) String status,
        @Schema(description = "신청일(미신청 상태에서는 입력 불가)", example = "2026-11-02") LocalDate requestedDate,
        @Schema(description = "승인일(승인 상태에서만 입력 가능, 신청일 이후)") LocalDate approvedDate,
        @Schema(description = "필요일(투입 전까지 승인되어야 하는 날)", example = "2026-11-09") LocalDate targetDate,
        @Schema(description = "담당", example = "인프라 담당") @Size(max = 100) String owner,
        @Schema(description = "비고") @Size(max = 1000) String note) {
}
