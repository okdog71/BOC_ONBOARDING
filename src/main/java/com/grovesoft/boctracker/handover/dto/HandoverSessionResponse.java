package com.grovesoft.boctracker.handover.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "인수인계(KT) 세션 응답")
public record HandoverSessionResponse(
        @Schema(description = "ID", example = "1") Long id,
        @Schema(description = "코드(불변 식별자)", example = "W1.1") String code,
        @Schema(description = "주차 내 순서(No.)", example = "1") int no,
        @Schema(description = "주차", example = "W1") String week,
        @Schema(description = "구분", example = "공통") String category,
        @Schema(description = "도메인", example = "전체 개요") String domain,
        @Schema(description = "주제", example = "KEC/BOC 시스템 개론") String topic,
        @Schema(description = "KT 단계", example = "설명·병행") String ktStage,
        @Schema(description = "인계자") String handoverFrom,
        @Schema(description = "인수자") String handoverTo,
        @Schema(description = "진행 방식") String method,
        @Schema(description = "산출물") String output,
        @Schema(description = "선행 조건(원문)") String precondition,
        @Schema(description = "예정일", example = "2026-11-02") LocalDate plannedDate,
        @Schema(description = "완료일(서버 기록)", example = "2026-11-03") LocalDate completedDate,
        @Schema(description = "상태", example = "미착수") String status,
        @Schema(description = "지연 여부(예정일 경과 & 미완료)", example = "false") boolean overdue,
        @Schema(description = "연결된 마일스톤 코드", example = "HO.1", nullable = true) String milestoneCode,
        @Schema(description = "연결된 마일스톤 이름", nullable = true) String milestoneName,
        @Schema(description = "선행 수급 산출물(②)") List<SupplyRef> supplyDocs,
        @Schema(description = "선행 접근권한(③)") List<AccessRef> accessItems,
        @Schema(description = "시작 전 미충족 항목(미수령 수급·미승인 접근·미완료 세션). 차단하지 않고 경고") List<String> blockers,
        @Schema(description = "데이터 품질 경고") List<String> warnings,
        @Schema(description = "비고") String note,
        @Schema(description = "생성 시각") LocalDateTime createdAt,
        @Schema(description = "수정 시각") LocalDateTime updatedAt,
        @Schema(description = "등록자(로그인 ID)", example = "level9") String createdBy,
        @Schema(description = "수정자(로그인 ID)", example = "level9") String updatedBy) {

    @Schema(description = "선행 수급 산출물 참조")
    public record SupplyRef(String code, String name, String status, boolean ready) {
    }

    @Schema(description = "선행 접근권한 참조")
    public record AccessRef(String code, String target, String status, boolean ready) {
    }
}
