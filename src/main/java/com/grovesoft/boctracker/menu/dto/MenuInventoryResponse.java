package com.grovesoft.boctracker.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "메뉴 인벤토리 응답")
public record MenuInventoryResponse(
        @Schema(description = "ID", example = "1") Long id,
        @Schema(description = "GNB", example = "운영") String gnb,
        @Schema(description = "메뉴", example = "쿠폰 관리") String menu,
        @Schema(description = "서브메뉴", example = "쿠폰 등록") String submenu,
        @Schema(description = "경로", example = "/redeem/coupon") String path,
        @Schema(description = "도메인", example = "redeem") String domain,
        @Schema(description = "백엔드 서비스", example = "redeem-api") String backendService,
        @Schema(description = "데이터 저장소", example = "MySQL") String dataStore,
        @Schema(description = "외부 연동", example = "결제 게이트웨이") String externalLink,
        @Schema(description = "권한", example = "운영자") String permission,
        @Schema(description = "세대(A/B/혼용/미확인/-)", example = "A") String generation,
        @Schema(description = "우선순위(상/중/하)", example = "상") String priority,
        @Schema(description = "알려진 이슈(IssueQueue 식별 문자열)", example = "KEC-01") String knownIssue,
        @Schema(description = "dev 확인(확인/dev미노출/mock/미확인)", example = "확인") String devCheck,
        @Schema(description = "담당", example = "BOC 리드") String owner,
        @Schema(description = "분석 상태(미착수/진행중/완료/보류)", example = "미착수") String analysisStatus,
        @Schema(description = "비고") String note,
        @Schema(description = "생성 시각", example = "2026-11-02T09:30:00") LocalDateTime createdAt,
        @Schema(description = "수정 시각", example = "2026-11-02T09:30:00") LocalDateTime updatedAt) {
}
