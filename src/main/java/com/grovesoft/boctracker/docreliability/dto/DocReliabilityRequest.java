package com.grovesoft.boctracker.docreliability.dto;

import com.grovesoft.boctracker.common.validation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "문서 신뢰도 생성/수정 요청")
public record DocReliabilityRequest(
        @Schema(description = "문서명", example = "IA 정의서") @NotBlank @Size(max = 255) String docName,
        @Schema(description = "출처", example = "Confluence") @Size(max = 500) String source,
        @Schema(description = "최종 수정", example = "2026-09") @Size(max = 100) String lastUpdated,
        @Schema(description = "최신성(현행/레거시/혼재/불명/미확인)", example = "현행") @EnumValue(allowed = {"현행", "레거시", "혼재", "불명", "미확인"}) @Size(max = 20) String freshness,
        @Schema(description = "코드 적합도(높음/보통/낮음/미확인)", example = "높음") @EnumValue(allowed = {"높음", "보통", "낮음", "미확인"}) @Size(max = 20) String codeFit,
        @Schema(description = "등급(A/B/C)", example = "A") @EnumValue(allowed = {"A", "B", "C"}) @Size(max = 10) String grade,
        @Schema(description = "검증 방법", example = "코드 대조") @Size(max = 1000) String verifyMethod,
        @Schema(description = "차이", example = "신규 메뉴 누락") @Size(max = 1000) String gap,
        @Schema(description = "담당") @Size(max = 100) String owner,
        @Schema(description = "비고") @Size(max = 1000) String note) {
}
