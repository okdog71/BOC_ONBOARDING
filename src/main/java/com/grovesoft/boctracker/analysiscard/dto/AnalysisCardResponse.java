package com.grovesoft.boctracker.analysiscard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "메뉴별 분석 카드 응답")
public record AnalysisCardResponse(
        @Schema(description = "ID", example = "1") Long id,
        @Schema(description = "메뉴명", example = "쿠폰 등록") String menuName,
        @Schema(description = "경로", example = "/redeem/coupon") String path,
        @Schema(description = "도메인", example = "redeem") String domain,
        @Schema(description = "백엔드 서비스", example = "redeem-api") String backendService,
        @Schema(description = "세대(A/B/혼용/미확인/-)", example = "A") String generation,
        @Schema(description = "요약", example = "쿠폰 코드를 등록한다") String summary,
        @Schema(description = "시나리오", example = "운영자가 코드를 입력하고 검증 후 저장") String scenario,
        @Schema(description = "입력", example = "쿠폰 코드") String input,
        @Schema(description = "출력", example = "등록 결과") String output,
        @Schema(description = "백엔드 매핑", example = "POST /coupons") String backendMapping,
        @Schema(description = "데이터 저장소", example = "MySQL") String dataStore,
        @Schema(description = "외부 연동", example = "결제 게이트웨이") String externalLink,
        @Schema(description = "권한", example = "운영자") String permission,
        @Schema(description = "관련 프로세스", example = "쿠폰 리딤") String relatedProcess,
        @Schema(description = "알려진 이슈", example = "KEC-01") String knownIssue,
        @Schema(description = "dev 확인(확인/dev미노출/mock/미확인)", example = "확인") String devCheck,
        @Schema(description = "리스크", example = "중복 등록") String risk,
        @Schema(description = "담당", example = "BOC 리드") String owner,
        @Schema(description = "분석일", example = "2026-11-05") LocalDate analyzedDate,
        @Schema(description = "상태(미착수/진행중/완료/보류)", example = "미착수") String status,
        @Schema(description = "생성 시각", example = "2026-11-02T09:30:00") LocalDateTime createdAt,
        @Schema(description = "수정 시각", example = "2026-11-02T09:30:00") LocalDateTime updatedAt) {
}
