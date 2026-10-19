package com.grovesoft.boctracker.link;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "마일스톤 산출물이 가리키는 메뉴(②③⑤)의 실제 진행 현황")
public record LinkedProgress(
        @Schema(description = "시트 기호", example = "②") String sheet,
        @Schema(description = "대상 메뉴", example = "수급 산출물") String label,
        @Schema(description = "프런트 라우트", example = "/supply-docs") String route,
        @Schema(description = "집계 기준", example = "수령/해당없음") String metric,
        @Schema(description = "충족 수", example = "3") long done,
        @Schema(description = "전체 수", example = "27") long total) {
}
