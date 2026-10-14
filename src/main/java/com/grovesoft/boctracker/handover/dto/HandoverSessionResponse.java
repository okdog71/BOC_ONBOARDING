package com.grovesoft.boctracker.handover.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "인수인계(KT) 세션 응답")
public record HandoverSessionResponse(
        @Schema(description = "ID", example = "1") Long id,
        @Schema(description = "주차(W1~W4)", example = "W1") String week,
        @Schema(description = "구분(공통/KEC/BOC)", example = "공통") String category,
        @Schema(description = "도메인", example = "전체 개요") String domain,
        @Schema(description = "주제", example = "KEC/BOC 시스템 개론·아키텍처·도메인 맵") String topic,
        @Schema(description = "KT 단계(설명/관찰/병행/독립, 복합 표기 허용)", example = "설명·병행") String ktStage,
        @Schema(description = "인계자", example = "현업 담당자") String handoverFrom,
        @Schema(description = "인수자", example = "BOC 리드") String handoverTo,
        @Schema(description = "진행 방식", example = "세션+문서") String method,
        @Schema(description = "산출물", example = "아키텍처 이해 메모") String output,
        @Schema(description = "선행 조건", example = "수급: 시스템구조문서(②)") String precondition,
        @Schema(description = "예정일", example = "2026-11-02") LocalDate plannedDate,
        @Schema(description = "상태(미착수/예정/진행중/완료/보류)", example = "미착수") String status,
        @Schema(description = "비고", example = "사전 자료 공유 필요") String note,
        @Schema(description = "생성 시각", example = "2026-11-02T09:30:00") LocalDateTime createdAt,
        @Schema(description = "수정 시각", example = "2026-11-02T09:30:00") LocalDateTime updatedAt) {
}
