package com.grovesoft.boctracker.milestone.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "단계(Phase) 요약. 첫 등장 순서(id 최소값 순)로 정렬되어 내려간다.")
public record PhaseResponse(
        @Schema(description = "단계명", example = "P0. 수급·준비") String phase,
        @Schema(description = "마일스톤 수", example = "5") long total,
        @Schema(description = "완료 수", example = "2") long done) {
}
