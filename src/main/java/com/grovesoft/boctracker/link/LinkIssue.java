package com.grovesoft.boctracker.link;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "메뉴·문서 간 링크 점검 결과 한 건")
public record LinkIssue(
        @Schema(description = "심각도(WARN: 연결 누락/불일치)", example = "WARN") String severity,
        @Schema(description = "출처 항목 코드", example = "W1.2") String source,
        @Schema(description = "내용", example = "선행 조건에 '수급:'이 있으나 연결된 수급 산출물이 없습니다.") String message) {

    @Schema(description = "링크 점검 응답")
    public record Report(
            @Schema(description = "점검한 연결 종류별 건수") List<Check> checks,
            @Schema(description = "문제 항목(없으면 모든 링크 정상)") List<LinkIssue> issues) {
    }

    @Schema(description = "점검 항목 요약")
    public record Check(
            @Schema(description = "점검 이름", example = "인수인계 → 마일스톤") String name,
            @Schema(description = "점검 대상 수", example = "24") long checked,
            @Schema(description = "문제 수", example = "0") long problems) {
    }
}
