package com.grovesoft.boctracker.summary.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "전체 대시보드 집계(⑭요약 대응). 도메인별 {total, byStatus, byPriority}")
public record SummaryResponse(
        Section milestone,
        Section handover,
        Section catchup,
        Section supply,
        Section access,
        Section menu,
        Section issue,
        Section raci,
        Section review,
        Section analysisCard,
        Section megaProcess,
        Section interfaceLink,
        Section docReliability,
        Section techDebt,
        Section handoverQuestion) {

    /**
     * @param total      전체 건수
     * @param byStatus   상태별 건수(허용값은 0 건이어도 포함, 순서는 코드값 표 순). status 가 없는 트래커는 생략
     * @param byPriority 우선순위별 건수(menu·techDebt·handoverQuestion). priority 미지정 행이 있으면 "미지정" 키 추가
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Section(long total, Map<String, Long> byStatus, Map<String, Long> byPriority) {
    }
}
