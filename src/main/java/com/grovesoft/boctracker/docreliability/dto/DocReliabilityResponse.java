package com.grovesoft.boctracker.docreliability.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "문서 신뢰도 응답")
public record DocReliabilityResponse(
        @Schema(description = "ID", example = "1") Long id,
        @Schema(description = "문서명", example = "IA 정의서") String docName,
        @Schema(description = "출처", example = "Confluence") String source,
        @Schema(description = "최종 수정", example = "2026-09") String lastUpdated,
        @Schema(description = "최신성(현행/레거시/혼재/불명/미확인)", example = "현행") String freshness,
        @Schema(description = "코드 적합도(높음/보통/낮음/미확인)", example = "높음") String codeFit,
        @Schema(description = "등급(A/B/C)", example = "A") String grade,
        @Schema(description = "검증 방법", example = "코드 대조") String verifyMethod,
        @Schema(description = "차이", example = "신규 메뉴 누락") String gap,
        @Schema(description = "담당") String owner,
        @Schema(description = "비고") String note,
        @Schema(description = "생성 시각", example = "2026-11-02T09:30:00") LocalDateTime createdAt,
        @Schema(description = "수정 시각", example = "2026-11-02T09:30:00") LocalDateTime updatedAt) {
}
