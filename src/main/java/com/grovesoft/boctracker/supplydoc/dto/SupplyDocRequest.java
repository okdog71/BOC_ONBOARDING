package com.grovesoft.boctracker.supplydoc.dto;

import com.grovesoft.boctracker.common.validation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "수급 산출물 생성/수정 요청")
public record SupplyDocRequest(
        @Schema(description = "코드. 생성 시 생략하면 구분 알파벳 접두로 자동 부여(예: A.6). 부여 후 변경 불가", example = "A.1")
        @Size(max = 50) String code,
        @Schema(description = "구분", example = "A. 기획·화면") @NotBlank @Size(max = 100) String category,
        @Schema(description = "산출물명", example = "화면 스토리보드") @NotBlank @Size(max = 255) String name,
        @Schema(description = "기대 내용", example = "메뉴별 화면 정의") @Size(max = 1000) String expectedContent,
        @Schema(description = "활용 목적", example = "메뉴 분석 기준") @Size(max = 1000) String usagePurpose,
        @Schema(description = "존재 여부(있음/없음/부분/미확인)", example = "있음") @EnumValue(allowed = {"있음", "없음", "부분", "미확인"}) String existence,
        @Schema(description = "위치", example = "Confluence/BOC/스토리보드") @Size(max = 500) String location,
        @Schema(description = "최신성(현행/레거시/혼재/불명/미확인)", example = "혼재") @EnumValue(allowed = {"현행", "레거시", "혼재", "불명", "미확인"}) String freshness,
        @Schema(description = "담당", example = "기획 담당") @Size(max = 100) String owner,
        @Schema(description = "수급 상태(미요청/요청/수령/해당없음). 요청/수령일은 상태 전이 시 서버가 기록", example = "미요청") @EnumValue(allowed = {"미요청", "요청", "수령", "해당없음"}) String supplyStatus,
        @Schema(description = "필요일(이 날까지 수령해야 함)", example = "2026-11-10") LocalDate targetDate,
        @Schema(description = "비고") @Size(max = 1000) String note) {
}
