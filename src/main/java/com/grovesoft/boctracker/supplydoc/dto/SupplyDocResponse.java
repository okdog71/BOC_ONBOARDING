package com.grovesoft.boctracker.supplydoc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "수급 산출물 응답")
public record SupplyDocResponse(
        @Schema(description = "ID", example = "1") Long id,
        @Schema(description = "구분", example = "A. 기획·화면") String category,
        @Schema(description = "산출물명", example = "화면 스토리보드") String name,
        @Schema(description = "기대 내용", example = "메뉴별 화면 정의") String expectedContent,
        @Schema(description = "활용 목적", example = "메뉴 분석 기준") String usagePurpose,
        @Schema(description = "존재 여부(있음/없음/부분/미확인)", example = "있음") String existence,
        @Schema(description = "위치", example = "Confluence/BOC/스토리보드") String location,
        @Schema(description = "최신성(현행/레거시/혼재/불명/미확인)", example = "혼재") String freshness,
        @Schema(description = "담당", example = "기획 담당") String owner,
        @Schema(description = "수급 상태(미요청/요청/수령/해당없음)", example = "미요청") String supplyStatus,
        @Schema(description = "비고") String note,
        @Schema(description = "생성 시각", example = "2026-11-02T09:30:00") LocalDateTime createdAt,
        @Schema(description = "수정 시각", example = "2026-11-02T09:30:00") LocalDateTime updatedAt) {
}
