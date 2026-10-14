package com.grovesoft.boctracker.analysiscard.dto;

import com.grovesoft.boctracker.common.validation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "메뉴별 분석 카드 생성/수정 요청")
public record AnalysisCardRequest(
        @Schema(description = "메뉴명", example = "쿠폰 등록") @NotBlank @Size(max = 255) String menuName,
        @Schema(description = "경로", example = "/redeem/coupon") @Size(max = 500) String path,
        @Schema(description = "도메인", example = "redeem") @Size(max = 100) String domain,
        @Schema(description = "백엔드 서비스", example = "redeem-api") @Size(max = 500) String backendService,
        @Schema(description = "세대(A/B/혼용/미확인/-)", example = "A") @EnumValue(allowed = {"A", "B", "혼용", "미확인", "-"}) @Size(max = 20) String generation,
        @Schema(description = "요약", example = "쿠폰 코드를 등록한다") @Size(max = 1000) String summary,
        @Schema(description = "시나리오", example = "운영자가 코드를 입력하고 검증 후 저장") @Size(max = 2000) String scenario,
        @Schema(description = "입력", example = "쿠폰 코드") @Size(max = 1000) String input,
        @Schema(description = "출력", example = "등록 결과") @Size(max = 1000) String output,
        @Schema(description = "백엔드 매핑", example = "POST /coupons") @Size(max = 1000) String backendMapping,
        @Schema(description = "데이터 저장소", example = "MySQL") @Size(max = 500) String dataStore,
        @Schema(description = "외부 연동", example = "결제 게이트웨이") @Size(max = 500) String externalLink,
        @Schema(description = "권한", example = "운영자") @Size(max = 500) String permission,
        @Schema(description = "관련 프로세스", example = "쿠폰 리딤") @Size(max = 500) String relatedProcess,
        @Schema(description = "알려진 이슈", example = "KEC-01") @Size(max = 500) String knownIssue,
        @Schema(description = "dev 확인(확인/dev미노출/mock/미확인)", example = "확인") @EnumValue(allowed = {"확인", "dev미노출", "mock", "미확인"}) @Size(max = 20) String devCheck,
        @Schema(description = "리스크", example = "중복 등록") @Size(max = 1000) String risk,
        @Schema(description = "담당", example = "BOC 리드") @Size(max = 100) String owner,
        @Schema(description = "분석일", example = "2026-11-05") LocalDate analyzedDate,
        @Schema(description = "상태(미착수/진행중/완료/보류)", example = "미착수") @EnumValue(allowed = {"미착수", "진행중", "완료", "보류"}) @Size(max = 20) String status) {
}
