package com.grovesoft.boctracker.common.attachment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "첨부 파일 정보")
public record AttachmentResponse(
        @Schema(description = "파일 ID", example = "1") Long id,
        @Schema(description = "원본 파일명", example = "응답-메일.pdf") String name,
        @Schema(description = "콘텐츠 타입", example = "application/pdf") String contentType,
        @Schema(description = "크기(바이트)", example = "20480") long size,
        @Schema(description = "다운로드 경로", example = "/api/tech-debts/1/files/3") String downloadUrl,
        @Schema(description = "올린 시각") LocalDateTime uploadedAt) {
}
