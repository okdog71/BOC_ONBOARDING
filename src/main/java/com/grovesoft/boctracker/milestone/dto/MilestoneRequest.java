package com.grovesoft.boctracker.milestone.dto;

import com.grovesoft.boctracker.common.validation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "마일스톤 생성/수정 요청")
public record MilestoneRequest(
        @Schema(description = "단계", example = "P0. 수급·준비")
        @NotBlank @Size(max = 100) String phase,
        @Schema(description = "마일스톤 코드(유일)", example = "M0.1")
        @NotBlank @Size(max = 50) String code,
        @Schema(description = "마일스톤명", example = "수급 산출물 확인")
        @NotBlank @Size(max = 255) String name,
        @Schema(description = "목표", example = "기존 BOC 운영 산출물의 존재/위치/최신성/owner 확인")
        @Size(max = 1000) String goal,
        @Schema(description = "체크포인트", example = "스토리보드·업무흐름도·IA·Jira·Confluence·Git 존재 여부")
        @Size(max = 1000) String checkpoint,
        @Schema(description = "산출물", example = "수급 산출물 확인표(②)")
        @Size(max = 1000) String output,
        @Schema(description = "선행 조건", example = "현업 인터뷰·접근 승인")
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
        @Size(max = 1000) String note) {
}
