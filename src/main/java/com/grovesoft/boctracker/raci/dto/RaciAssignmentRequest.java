package com.grovesoft.boctracker.raci.dto;

import com.grovesoft.boctracker.common.validation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "RACI 배정 생성/수정 요청")
public record RaciAssignmentRequest(
        @Schema(description = "도메인 그룹", example = "redeem/order") @NotBlank @Size(max = 255) String domainGroup,
        @Schema(description = "범위", example = "쿠폰·주문 도메인") @Size(max = 1000) String scope,
        @Schema(description = "복잡도(상/중/하)", example = "상") @EnumValue(allowed = {"상", "중", "하"}) @Size(max = 10) String complexity,
        @Schema(description = "규모(상/중/하)", example = "중") @EnumValue(allowed = {"상", "중", "하"}) @Size(max = 10) String scale,
        @Schema(description = "세대(A/B/혼용/미확인/-)", example = "B") @EnumValue(allowed = {"A", "B", "혼용", "미확인", "-"}) @Size(max = 20) String generation,
        @Schema(description = "담당(R)", example = "담당자 A") @Size(max = 255) String responsible,
        @Schema(description = "백업", example = "담당자 B") @Size(max = 255) String backup,
        @Schema(description = "리뷰어", example = "리드") @Size(max = 255) String reviewer,
        @Schema(description = "메뉴 수", example = "12") @Min(0) Integer menuCount,
        @Schema(description = "상태(미착수/진행중/완료/보류)", example = "미착수") @EnumValue(allowed = {"미착수", "진행중", "완료", "보류"}) @Size(max = 20) String status,
        @Schema(description = "목표일", example = "2026-12-15") LocalDate targetDate,
        @Schema(description = "비고") @Size(max = 1000) String note) {
}
