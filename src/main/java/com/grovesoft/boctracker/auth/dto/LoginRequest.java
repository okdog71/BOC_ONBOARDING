package com.grovesoft.boctracker.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "로그인 요청")
public record LoginRequest(
        @Schema(description = "로그인 ID", example = "level9") @NotBlank @Size(max = 30) String loginId,
        @Schema(description = "비밀번호") @NotBlank @Size(max = 72) String password) {
}
