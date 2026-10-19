package com.grovesoft.boctracker.handover.dto;

import com.grovesoft.boctracker.common.validation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "인수인계(KT) 세션 생성/수정 요청")
public record HandoverSessionRequest(
        @Schema(description = "코드. 생성 시 생략하면 {주차}.{순번}으로 자동 부여. 부여 후 변경 불가", example = "W1.1")
        @Size(max = 50) String code,
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
        @Schema(description = "선행 조건(원문 메모). 정규화된 선행은 supplyDocCodes/accessItemCodes 로 지정", example = "수급: 시스템구조문서(②)")
        @Size(max = 1000) String precondition,
        @Schema(description = "예정일", example = "2026-11-02")
        LocalDate plannedDate,
        @Schema(description = "상태(미착수/예정/진행중/완료/보류). 생략 시 미착수. 완료일은 서버가 기록", example = "미착수")
        @EnumValue(allowed = {"미착수", "예정", "진행중", "완료", "보류"}) String status,
        @Schema(description = "비고", example = "사전 자료 공유 필요")
        @Size(max = 1000) String note,
        @Schema(description = "연결할 인수인계 마일스톤 코드(없으면 연결 해제)", example = "HO.1")
        @Size(max = 50) String milestoneCode,
        @Schema(description = "선행으로 요구하는 수급 산출물 코드(②). PUT 은 전체 교체", example = "[\"B.1\"]")
        List<@Size(max = 50) String> supplyDocCodes,
        @Schema(description = "선행으로 요구하는 접근권한 코드(③). PUT 은 전체 교체", example = "[\"AC.8\"]")
        List<@Size(max = 50) String> accessItemCodes) {
}
