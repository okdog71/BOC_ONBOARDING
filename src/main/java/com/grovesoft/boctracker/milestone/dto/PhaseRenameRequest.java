package com.grovesoft.boctracker.milestone.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "단계명 일괄 변경 요청. 변경할 이름이 이미 있으면 두 단계가 병합된다.")
public record PhaseRenameRequest(
        @Schema(description = "현재 단계명", example = "P1. IA") @NotBlank @Size(max = 100) String from,
        @Schema(description = "새 단계명", example = "P1. IA 분석") @NotBlank @Size(max = 100) String to) {
}
