package com.grovesoft.boctracker.issue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "이슈·미확인 큐 응답")
public record IssueQueueResponse(
        @Schema(description = "ID", example = "1") Long id,
        @Schema(description = "유형(이슈/미확인/리스크)", example = "이슈") String type,
        @Schema(description = "항목", example = "KEC-01") String item,
        @Schema(description = "내용", example = "쿠폰 중복 등록 허용 여부 불명확") String content,
        @Schema(description = "영향", example = "정산 오류 가능") String impact,
        @Schema(description = "근거", example = "dev 화면 확인") String basis,
        @Schema(description = "확인 대상", example = "현업 담당자") String confirmWith,
        @Schema(description = "담당", example = "BOC 리드") String owner,
        @Schema(description = "상태(미착수/확인중/진행중/완료/보류)", example = "미착수") String status,
        @Schema(description = "목표일", example = "2026-11-30") LocalDate targetDate,
        @Schema(description = "조치", example = "현업 인터뷰") String action,
        @Schema(description = "비고") String note,
        @Schema(description = "생성 시각", example = "2026-11-02T09:30:00") LocalDateTime createdAt,
        @Schema(description = "수정 시각", example = "2026-11-02T09:30:00") LocalDateTime updatedAt,
        @Schema(description = "등록자(로그인 ID)", example = "level9") String createdBy,
        @Schema(description = "수정자(로그인 ID)", example = "level9") String updatedBy) {
}
