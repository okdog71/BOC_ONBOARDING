package com.grovesoft.boctracker.handover.dto;

import com.grovesoft.boctracker.common.validation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "인수인계(KT) 세션 생성/수정 요청")
public record HandoverSessionRequest(
        @Schema(description = "주차(W1~W4)", example = "W1")
        @NotBlank @EnumValue(allowed = {"W1", "W2", "W3", "W4"}, nullable = false) String week,
        @Schema(description = "구분(공통/KEC/BOC)", example = "공통")
        @NotBlank @EnumValue(allowed = {"공통", "KEC", "BOC"}, nullable = false) String category,
        @Schema(description = "도메인", example = "전체 개요")
        @Size(max = 255) String domain,
        @Schema(description = "주제", example = "KEC/BOC 시스템 개론·아키텍처·도메인 맵")
        @NotBlank @Size(max = 1000) String topic,
        @Schema(description = "KT 단계(설명/관찰/병행/독립, 복합 표기 허용: 설명·병행)", example = "설명·병행")
        @EnumValue(allowed = {"설명", "관찰", "병행", "독립"}, delimiter = "·") @Size(max = 50) String ktStage,
        @Schema(description = "인계자", example = "현업 담당자")
        @Size(max = 255) String handoverFrom,
        @Schema(description = "인수자", example = "BOC 리드")
        @Size(max = 255) String handoverTo,
        @Schema(description = "진행 방식", example = "세션+문서")
        @Size(max = 255) String method,
        @Schema(description = "산출물", example = "아키텍처 이해 메모")
        @Size(max = 1000) String output,
        @Schema(description = "선행 조건", example = "수급: 시스템구조문서(②)")
        @Size(max = 1000) String precondition,
        @Schema(description = "예정일", example = "2026-11-02")
        LocalDate plannedDate,
        @Schema(description = "상태(미착수/예정/진행중/완료/보류). 생략 시 미착수", example = "미착수")
        @EnumValue(allowed = {"미착수", "예정", "진행중", "완료", "보류"}) String status,
        @Schema(description = "비고", example = "사전 자료 공유 필요")
        @Size(max = 1000) String note) {
}
