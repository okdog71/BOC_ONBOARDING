package com.grovesoft.boctracker.milestone.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.grovesoft.boctracker.link.LinkedProgress;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "마일스톤 응답")
public record MilestoneResponse(
        @Schema(description = "ID", example = "1") Long id,
        @Schema(description = "단계", example = "P0. 수급·준비") String phase,
        @Schema(description = "마일스톤 코드(유일·불변 식별자)", example = "P0.1") String code,
        @Schema(description = "단계 내 순서(No., 1부터)", example = "1") int no,
        @Schema(description = "마일스톤명", example = "수급 산출물 확인") String name,
        @Schema(description = "목표", example = "기존 BOC 운영 산출물의 존재/위치/최신성/owner 확인") String goal,
        @Schema(description = "체크포인트", example = "스토리보드·업무흐름도·IA·Jira·Confluence·Git 존재 여부") String checkpoint,
        @Schema(description = "산출물", example = "수급 산출물 확인표(②)") String output,
        @Schema(description = "선행 조건(원문)", example = "현업 인터뷰·접근 승인") String precondition,
        @Schema(description = "담당", example = "BOC 리드") String owner,
        @Schema(description = "소요 기간(주)", example = "2") String durationWeeks,
        @Schema(description = "상태(미착수/진행중/완료/보류)", example = "미착수") String status,
        @Schema(description = "완료 기준", example = "수급 목록 확정·신뢰도 등급 부여") String doneCriteria,
        @Schema(description = "비고", example = "투입 전 착수 권장") String note,
        @Schema(description = "목표일", example = "2026-11-15") LocalDate targetDate,
        @Schema(description = "완료일(서버 관리: 완료 전환 시 기록)", example = "2026-11-10") LocalDate completedDate,
        @Schema(description = "지연 여부(목표일이 지났고 완료가 아님)", example = "false") boolean overdue,
        @Schema(description = "선행 마일스톤 코드(precondition 에서 해석된 FK)", example = "[\"P0.1\", \"P0.2\"]")
        List<String> predecessorCodes,
        @Schema(description = "선행 중 아직 완료되지 않은 마일스톤 코드(시작/완료 전 경고용)", example = "[\"P0.2\"]")
        List<String> blockedBy,
        @Schema(description = "체크리스트 진척") Progress progress,
        @Schema(description = "체크리스트 항목") List<CheckItemResponse> checkItems,
        @Schema(description = "산출물이 가리키는 메뉴(②③⑤)의 실제 진행 현황") List<LinkedProgress> links,
        @Schema(description = "생성 시각", example = "2026-11-02T09:30:00") LocalDateTime createdAt,
        @Schema(description = "수정 시각", example = "2026-11-02T09:30:00") LocalDateTime updatedAt) {

    @Schema(description = "체크리스트 진척. 항목이 없으면 total=0, percent=null")
    public record Progress(
            @Schema(description = "완료 항목 수", example = "2") int done,
            @Schema(description = "전체 항목 수", example = "5") int total,
            @Schema(description = "완료율(%)", example = "40", nullable = true) Integer percent) {
    }

    @Schema(description = "체크리스트 항목")
    public record CheckItemResponse(
            @Schema(description = "항목 ID", example = "10") Long id,
            @Schema(description = "내용", example = "Jira 접근 확인") String title,
            @Schema(description = "완료 여부", example = "false") boolean done) {
    }
}
