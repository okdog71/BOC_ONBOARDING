package com.grovesoft.boctracker.menu.dto;

import com.grovesoft.boctracker.common.validation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "메뉴 인벤토리 생성/수정 요청")
public record MenuInventoryRequest(
        @Schema(description = "GNB", example = "운영") @Size(max = 100) String gnb,
        @Schema(description = "메뉴", example = "쿠폰 관리") @NotBlank @Size(max = 255) String menu,
        @Schema(description = "서브메뉴", example = "쿠폰 등록") @Size(max = 255) String submenu,
        @Schema(description = "경로", example = "/redeem/coupon") @Size(max = 500) String path,
        @Schema(description = "도메인", example = "redeem") @Size(max = 100) String domain,
        @Schema(description = "백엔드 서비스", example = "redeem-api") @Size(max = 500) String backendService,
        @Schema(description = "데이터 저장소", example = "MySQL") @Size(max = 500) String dataStore,
        @Schema(description = "외부 연동", example = "결제 게이트웨이") @Size(max = 500) String externalLink,
        @Schema(description = "권한", example = "운영자") @Size(max = 500) String permission,
        @Schema(description = "세대(A/B/혼용/미확인/-)", example = "A") @EnumValue(allowed = {"A", "B", "혼용", "미확인", "-"}) @Size(max = 20) String generation,
        @Schema(description = "우선순위(상/중/하)", example = "상") @EnumValue(allowed = {"상", "중", "하"}) @Size(max = 10) String priority,
        @Schema(description = "알려진 이슈(IssueQueue 식별 문자열)", example = "KEC-01") @Size(max = 500) String knownIssue,
        @Schema(description = "dev 확인(확인/dev미노출/mock/미확인)", example = "확인") @EnumValue(allowed = {"확인", "dev미노출", "mock", "미확인"}) @Size(max = 20) String devCheck,
        @Schema(description = "담당", example = "BOC 리드") @Size(max = 100) String owner,
        @Schema(description = "분석 상태(미착수/진행중/완료/보류)", example = "미착수") @EnumValue(allowed = {"미착수", "진행중", "완료", "보류"}) @Size(max = 20) String analysisStatus,
        @Schema(description = "비고") @Size(max = 1000) String note) {
}
