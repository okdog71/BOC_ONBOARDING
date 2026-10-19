package com.grovesoft.boctracker.supplydoc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "수급 산출물 첨부 파일")
public record SupplyDocFileResponse(
        @Schema(description = "파일 ID", example = "1") Long id,
        @Schema(description = "원본 파일명", example = "배포구조.pdf") String name,
        @Schema(description = "콘텐츠 타입", example = "application/pdf") String contentType,
        @Schema(description = "크기(byte)", example = "102400") long size,
        @Schema(description = "다운로드 경로", example = "/api/supply-docs/1/files/3") String downloadUrl,
        @Schema(description = "업로드 시각") LocalDateTime uploadedAt) {
}
