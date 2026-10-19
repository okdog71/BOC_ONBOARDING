package com.grovesoft.boctracker.techdebt.dto;

import com.grovesoft.boctracker.common.validation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "기술부채 생성/수정 요청")
public record TechDebtRequest(
        @Schema(description = "코드. 생성 시 생략하면 시스템 접두로 자동 부여(예: KEC-19). 부여 후 변경 불가", example = "KEC-01")
        @Size(max = 50) @Pattern(regexp = "^[A-Za-z]+-\\d+$", message = "코드는 KEC-01 형식이어야 합니다") String code,
        @Schema(description = "우선순위(P1 즉시/P2 단기/P3 중기/P4 위생)", example = "P1")
        @EnumValue(allowed = {"P1", "P2", "P3", "P4"}, nullable = false) String priority,
        @Schema(description = "시스템(KEC/BOC/공통)", example = "KEC")
        @EnumValue(allowed = {"KEC", "BOC", "공통"}, nullable = false) String system,
        @Schema(description = "영역", example = "연동/장애") @Size(max = 100) String area,
        @Schema(description = "항목명", example = "enroll provider 매칭 회귀") @NotBlank @Size(max = 255) String name,
        @Schema(description = "내용 요약") @Size(max = 2000) String summary,
        @Schema(description = "리스크·영향") @Size(max = 1000) String impact,
        @Schema(description = "근거(문서·조항)", example = "KEC 가이드 §5-1") @Size(max = 500) String basis,
        @Schema(description = "대응(처리) 방안") @Size(max = 2000) String remedy,
        @Schema(description = "확인 필요처") @Size(max = 255) String confirmWith,
        @Schema(description = "담당. 완료 처리에는 필수") @Size(max = 100) String owner,
        @Schema(description = "상태(미착수/확인중/진행중/검증중/완료/보류). 생략하면 미착수. 완료일은 서버가 기록", example = "미착수")
        @EnumValue(allowed = {"미착수", "확인중", "진행중", "검증중", "완료", "보류"}) String status,
        @Schema(description = "검증 기준(SOW §11 연계)") @Size(max = 1000) String verifyCriteria,
        @Schema(description = "목표 시점", example = "2026-11-30") LocalDate targetDate,
        @Schema(description = "비고") @Size(max = 1000) String note) {
}
