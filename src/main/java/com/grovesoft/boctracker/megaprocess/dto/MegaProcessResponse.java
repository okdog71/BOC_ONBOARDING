package com.grovesoft.boctracker.megaprocess.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "메가프로세스 응답")
public record MegaProcessResponse(
        @Schema(description = "ID", example = "1") Long id,
        @Schema(description = "L0", example = "리딤/주문") String l0,
        @Schema(description = "L1", example = "쿠폰 리딤") String l1,
        @Schema(description = "L2", example = "코드 검증") String l2,
        @Schema(description = "L3") String l3,
        @Schema(description = "관련 메뉴", example = "리딤 > 쿠폰 등록") String relatedMenu,
        @Schema(description = "관련 서비스") String relatedService,
        @Schema(description = "외부 연동") String externalLink,
        @Schema(description = "산출 데이터") String outputData,
        @Schema(description = "비고") String note,
        @Schema(description = "생성 시각", example = "2026-11-02T09:30:00") LocalDateTime createdAt,
        @Schema(description = "수정 시각", example = "2026-11-02T09:30:00") LocalDateTime updatedAt) {
}
