package com.grovesoft.boctracker.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "사용자 등록·수정 요청. 비밀번호는 받지 않는다(등록 시 로그인 ID 와 같은 초기 비밀번호).")
public record UserRequest(
        @Schema(description = "로그인 ID(등록 시에만 사용, 수정 시 변경 불가)", example = "level9")
        @NotBlank @Pattern(regexp = "^[a-z0-9._-]{3,30}$", message = "영문 소문자·숫자·._- 3~30자") String loginId,
        @Schema(description = "소속", example = "grove") @NotBlank @Size(max = 50) String affiliation,
        @Schema(description = "이름", example = "박석준") @NotBlank @Size(max = 50) String name,
        @Schema(description = "투입일") LocalDate joinDate,
        @Schema(description = "철수일") LocalDate leaveDate,
        @Schema(description = "크래프톤 계정(ID)") @Size(max = 100) String kraftonAccount,
        @Schema(description = "연락처") @Size(max = 30) String phone,
        @Schema(description = "Grove 메일") @Size(max = 100) String groveMail,
        @Schema(description = "IP") @Size(max = 100) String ip,
        @Schema(description = "G메일") @Size(max = 100) String gmail,
        @Schema(description = "VPN 계정(ID)") @Size(max = 100) String vpn,
        @Schema(description = "BOC 계정(ID)") @Size(max = 100) String bocAccount,
        @Schema(description = "KEC 계정(ID)") @Size(max = 100) String kecAccount,
        @Schema(description = "AWS 계정(ID)") @Size(max = 100) String aws) {
}
