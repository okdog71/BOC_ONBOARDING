package com.grovesoft.boctracker.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "사용자 응답(비밀번호·해시는 포함하지 않는다)")
public record UserResponse(
        @Schema(description = "ID") Long id,
        @Schema(description = "로그인 ID") String loginId,
        @Schema(description = "소속") String affiliation,
        @Schema(description = "이름") String name,
        @Schema(description = "투입일") LocalDate joinDate,
        @Schema(description = "철수일") LocalDate leaveDate,
        @Schema(description = "크래프톤 계정") String kraftonAccount,
        @Schema(description = "연락처") String phone,
        @Schema(description = "Grove 메일") String groveMail,
        @Schema(description = "IP") String ip,
        @Schema(description = "G메일") String gmail,
        @Schema(description = "VPN") String vpn,
        @Schema(description = "BOC 계정") String bocAccount,
        @Schema(description = "KEC 계정") String kecAccount,
        @Schema(description = "AWS") String aws,
        @Schema(description = "초기 비밀번호 상태(변경 안내 대상)") boolean mustChangePassword,
        @Schema(description = "생성 시각") LocalDateTime createdAt,
        @Schema(description = "수정 시각") LocalDateTime updatedAt,
        @Schema(description = "등록자(로그인 ID)") String createdBy,
        @Schema(description = "수정자(로그인 ID)") String updatedBy) {
}
