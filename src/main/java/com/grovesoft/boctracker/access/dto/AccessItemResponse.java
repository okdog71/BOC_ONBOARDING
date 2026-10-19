package com.grovesoft.boctracker.access.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "접근권한 응답")
public record AccessItemResponse(
        @Schema(description = "ID", example = "1") Long id,
        @Schema(description = "코드(불변 식별자)", example = "AC.1") String code,
        @Schema(description = "구분 내 순서(No.)", example = "1") int no,
        @Schema(description = "구분", example = "시스템") String category,
        @Schema(description = "대상", example = "BOC dev 콘솔") String target,
        @Schema(description = "용도", example = "메뉴 전수·기능 분석") String usage,
        @Schema(description = "환경 범위", example = "dev 전체 메뉴") String envScope,
        @Schema(description = "신청 절차") String procedure,
        @Schema(description = "상태", example = "미신청") String status,
        @Schema(description = "신청일", example = "2026-11-02") LocalDate requestedDate,
        @Schema(description = "승인일", example = "2026-11-04") LocalDate approvedDate,
        @Schema(description = "필요일", example = "2026-11-09") LocalDate targetDate,
        @Schema(description = "신청→승인 소요일", example = "2", nullable = true) Long leadTimeDays,
        @Schema(description = "승인 완료 여부", example = "false") boolean ready,
        @Schema(description = "지연 여부(필요일 경과 & 미승인)", example = "false") boolean overdue,
        @Schema(description = "다른 접근의 전제가 되는 항목 여부(용도에 '접근 전제' 포함)", example = "false") boolean prerequisite,
        @Schema(description = "경고(차단하지 않음)") List<String> warnings,
        @Schema(description = "이 접근권한을 선행으로 요구하는 인수인계 세션 코드", example = "[\"W1.2\"]") List<String> requiredBySessions,
        @Schema(description = "담당", example = "인프라 담당") String owner,
        @Schema(description = "비고") String note,
        @Schema(description = "생성 시각") LocalDateTime createdAt,
        @Schema(description = "수정 시각") LocalDateTime updatedAt) {
}
