package com.grovesoft.boctracker.issue.dto;

import com.grovesoft.boctracker.common.validation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "이슈·미확인 큐 생성/수정 요청")
public record IssueQueueRequest(
        @Schema(description = "유형(이슈/미확인/리스크)", example = "이슈") @EnumValue(allowed = {"이슈", "미확인", "리스크"}) @Size(max = 20) String type,
        @Schema(description = "항목", example = "KEC-01") @NotBlank @Size(max = 255) String item,
        @Schema(description = "내용", example = "쿠폰 중복 등록 허용 여부 불명확") @Size(max = 1000) String content,
        @Schema(description = "영향", example = "정산 오류 가능") @Size(max = 1000) String impact,
        @Schema(description = "근거", example = "dev 화면 확인") @Size(max = 1000) String basis,
        @Schema(description = "확인 대상", example = "현업 담당자") @Size(max = 255) String confirmWith,
        @Schema(description = "담당", example = "BOC 리드") @Size(max = 100) String owner,
        @Schema(description = "상태(미착수/확인중/진행중/완료/보류)", example = "미착수") @EnumValue(allowed = {"미착수", "확인중", "진행중", "완료", "보류"}) @Size(max = 20) String status,
        @Schema(description = "목표일", example = "2026-11-30") LocalDate targetDate,
        @Schema(description = "조치", example = "현업 인터뷰") @Size(max = 1000) String action,
        @Schema(description = "비고") @Size(max = 1000) String note) {
}
