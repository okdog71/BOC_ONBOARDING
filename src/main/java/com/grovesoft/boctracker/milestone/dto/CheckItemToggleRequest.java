package com.grovesoft.boctracker.milestone.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "체크리스트 항목 완료 토글 요청")
public record CheckItemToggleRequest(@Schema(description = "완료 여부", example = "true") boolean done) {
}
