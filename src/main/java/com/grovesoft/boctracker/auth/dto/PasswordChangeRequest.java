package com.grovesoft.boctracker.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "비밀번호 변경 요청(본인)")
public record PasswordChangeRequest(
        @Schema(description = "현재 비밀번호") @NotBlank @Size(max = 72) String currentPassword,
        @Schema(description = "새 비밀번호(6~72자, 로그인 ID 와 달라야 함)") @NotBlank @Size(min = 6, max = 72) String newPassword) {
}
