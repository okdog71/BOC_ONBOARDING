package com.grovesoft.boctracker.milestone.dto;

import com.grovesoft.boctracker.common.validation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "마일스톤 생성/수정 요청")
public record MilestoneRequest(
        @Schema(description = "단계", example = "P0. 수급·준비")
        @NotBlank @Size(max = 100) String phase,
        @Schema(description = "마일스톤 코드. 생성 시 생략하면 단계 접두로 자동 부여(예: P1.5). 한번 부여되면 변경 불가", example = "P0.1")
        @Size(max = 50) String code,
        @Schema(description = "마일스톤명", example = "수급 산출물 확인")
        @NotBlank @Size(max = 255) String name,
        @Schema(description = "목표", example = "기존 BOC 운영 산출물의 존재/위치/최신성/owner 확인")
        @Size(max = 1000) String goal,
        @Schema(description = "체크포인트", example = "스토리보드·업무흐름도·IA·Jira·Confluence·Git 존재 여부")
        @Size(max = 1000) String checkpoint,
        @Schema(description = "산출물", example = "수급 산출물 확인표(②)")
        @Size(max = 1000) String output,
        @Schema(description = "선행 조건. 마일스톤 코드(쉼표/공백 구분, A~B 범위)는 선행 FK 로 해석된다", example = "P0.1,P0.2")
        @Size(max = 1000) String precondition,
        @Schema(description = "담당", example = "BOC 리드")
        @Size(max = 100) String owner,
        @Schema(description = "소요 기간(주)", example = "2")
        @Size(max = 50) String durationWeeks,
        @Schema(description = "상태(미착수/진행중/완료/보류). 생략 시 미착수", example = "미착수")
        @EnumValue(allowed = {"미착수", "진행중", "완료", "보류"}) String status,
        @Schema(description = "완료 기준", example = "수급 목록 확정·신뢰도 등급 부여")
        @Size(max = 1000) String doneCriteria,
        @Schema(description = "비고", example = "투입 전 착수 권장")
        @Size(max = 1000) String note,
        @Schema(description = "목표일", example = "2026-11-15")
        LocalDate targetDate) {
}
