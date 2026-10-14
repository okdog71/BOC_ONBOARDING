package com.grovesoft.boctracker.interfacelink.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "연동 인터페이스 응답")
public record InterfaceLinkResponse(
        @Schema(description = "ID", example = "1") Long id,
        @Schema(description = "시스템명", example = "결제 게이트웨이") String systemName,
        @Schema(description = "방향", example = "BOC→") String direction,
        @Schema(description = "인터페이스명", example = "결제 승인 API") String interfaceName,
        @Schema(description = "용도", example = "결제 승인 요청") String usage,
        @Schema(description = "인증 방식", example = "API Key") String auth,
        @Schema(description = "주요 데이터", example = "주문번호, 금액") String data,
        @Schema(description = "dev 확인(확인/dev미노출/mock/미확인)", example = "확인") String devCheck,
        @Schema(description = "비고") String note,
        @Schema(description = "생성 시각", example = "2026-11-02T09:30:00") LocalDateTime createdAt,
        @Schema(description = "수정 시각", example = "2026-11-02T09:30:00") LocalDateTime updatedAt) {
}
