package com.grovesoft.boctracker.auditlog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "변경 이력 1건 (누가·언제·어디서·무엇을·어떻게)")
public record AuditLogResponse(
        @Schema(description = "ID") Long id,
        @Schema(description = "언제") LocalDateTime actedAt,
        @Schema(description = "누가: 로그인 ID(자동 처리는 system)", example = "level9") String actor,
        @Schema(description = "누가: 이름(기록 당시)", example = "박석준") String actorName,
        @Schema(description = "어떻게: CREATE/UPDATE/DELETE", example = "UPDATE") String action,
        @Schema(description = "무엇을: 도메인", example = "기술부채") String domain,
        @Schema(description = "대상 ID") Long entityId,
        @Schema(description = "대상 코드", example = "KEC-01") String targetCode,
        @Schema(description = "대상 이름") String targetLabel,
        @Schema(description = "어떻게: 변경 내용(필드별 전/후 값. 등록은 입력값, 삭제는 삭제 직전 값)") List<Change> changes,
        @Schema(description = "어디서: 접속 IP") String clientIp,
        @Schema(description = "어디서: 요청(메서드 경로)", example = "PUT /api/tech-debts/1") String request) {

    @Schema(description = "필드 변경")
    public record Change(
            @Schema(description = "필드(영문 속성명)", example = "status") String field,
            @Schema(description = "이전 값") String from,
            @Schema(description = "이후 값") String to) {
    }
}
