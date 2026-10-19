package com.grovesoft.boctracker.supplydoc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "수급 산출물 응답")
public record SupplyDocResponse(
        @Schema(description = "ID", example = "1") Long id,
        @Schema(description = "코드(불변 식별자)", example = "A.1") String code,
        @Schema(description = "구분 내 순서(No.)", example = "1") int no,
        @Schema(description = "구분", example = "A. 기획·화면") String category,
        @Schema(description = "산출물명", example = "스토리보드/화면정의서") String name,
        @Schema(description = "기대 내용", example = "화면 레이아웃·요소·동작·예외 처리") String expectedContent,
        @Schema(description = "활용 목적", example = "운영자 UI·업무 의도 1차 파악") String usagePurpose,
        @Schema(description = "존재 여부", example = "있음") String existence,
        @Schema(description = "위치", example = "Confluence/BOC") String location,
        @Schema(description = "최신성", example = "혼재") String freshness,
        @Schema(description = "담당", example = "기획 담당") String owner,
        @Schema(description = "수급 상태", example = "미요청") String supplyStatus,
        @Schema(description = "필요일", example = "2026-11-10") LocalDate targetDate,
        @Schema(description = "요청일(서버 기록)", example = "2026-11-02") LocalDate requestedDate,
        @Schema(description = "수령일(서버 기록)", example = "2026-11-05") LocalDate receivedDate,
        @Schema(description = "수령 완료/해당없음 여부", example = "false") boolean ready,
        @Schema(description = "지연 여부(필요일 경과 & 미수령)", example = "false") boolean overdue,
        @Schema(description = "데이터 품질 경고(차단하지 않음)", example = "[\"수령했지만 위치가 비어 있습니다.\"]") List<String> warnings,
        @Schema(description = "이 산출물을 선행으로 요구하는 인수인계 세션 코드", example = "[\"W1.1\"]") List<String> requiredBySessions,
        @Schema(description = "첨부 파일(수령한 실제 파일)") List<SupplyDocFileResponse> files,
        @Schema(description = "비고") String note,
        @Schema(description = "생성 시각") LocalDateTime createdAt,
        @Schema(description = "수정 시각") LocalDateTime updatedAt) {
}
