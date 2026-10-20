package com.grovesoft.boctracker.auth;

import com.grovesoft.boctracker.auth.dto.LoginRequest;
import com.grovesoft.boctracker.auth.dto.PasswordChangeRequest;
import com.grovesoft.boctracker.common.docs.ApiTags;
import com.grovesoft.boctracker.common.web.ApiResponse;
import com.grovesoft.boctracker.user.AppUser;
import com.grovesoft.boctracker.user.UserMapper;
import com.grovesoft.boctracker.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = ApiTags.AUTH, description = "로그인·로그아웃 (세션 쿠키). 로그인 외 모든 /api 는 로그인이 필요하다")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;
    private final UserMapper mapper;

    @Operation(summary = "로그인", description = "성공 시 세션 쿠키 발급. 실패 401, 연속 5회 실패 시 1분 잠금 429")
    @PostMapping("/login")
    public ApiResponse<UserResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        AppUser u = service.authenticate(request.loginId(), request.password());
        HttpSession old = http.getSession(false);
        if (old != null) {
            old.invalidate(); // 세션 고정 공격 방지: 로그인마다 새 세션
        }
        http.getSession(true).setAttribute(AuthFilter.SESSION_KEY, new SessionUser(u.getId(), u.getLoginId(), u.getName()));
        return ApiResponse.ok(mapper.toResponse(u));
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest http) {
        HttpSession s = http.getSession(false);
        if (s != null) {
            s.invalidate();
        }
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "내 정보", description = "로그인 상태 확인. 미로그인 401")
    @GetMapping("/me")
    public ApiResponse<UserResponse> me() {
        AppUser u = service.find(requireUser().id());
        if (u == null) {
            throw unauthorized();
        }
        return ApiResponse.ok(mapper.toResponse(u));
    }

    @Operation(summary = "내 비밀번호 변경", description = "현재 비밀번호 확인 필요. 새 비밀번호 6자 이상, 로그인 ID 와 달라야 한다")
    @PutMapping("/password")
    public ApiResponse<UserResponse> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        return ApiResponse.ok(mapper.toResponse(
                service.changePassword(requireUser().id(), request.currentPassword(), request.newPassword())));
    }

    private SessionUser requireUser() {
        return CurrentUser.get().orElseThrow(AuthController::unauthorized);
    }

    private static AuthException unauthorized() {
        return new AuthException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "로그인이 필요합니다.");
    }
}
