package com.grovesoft.boctracker.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grovesoft.boctracker.common.web.ApiResponse;
import com.grovesoft.boctracker.common.web.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * /api/** 요청을 세션 로그인으로 보호한다(로그인 API 제외). 로그인 사용자는 요청 동안 CurrentUser 에 담긴다.
 * app.auth.enabled=false 이면 검사하지 않고 감사 값은 "system" 이 된다(테스트·점검용).
 */
@Component
public class AuthFilter extends OncePerRequestFilter {

    public static final String SESSION_KEY = "boc.user";
    private static final String LOGIN_PATH = "/api/auth/login";

    private final ObjectMapper objectMapper;
    private final boolean enabled;

    public AuthFilter(ObjectMapper objectMapper, @Value("${app.auth.enabled:true}") boolean enabled) {
        this.objectMapper = objectMapper;
        this.enabled = enabled;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !enabled || !path.startsWith("/api/") || path.equals(LOGIN_PATH);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Object user = session == null ? null : session.getAttribute(SESSION_KEY);
        if (!(user instanceof SessionUser su)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            objectMapper.writeValue(response.getWriter(),
                    ApiResponse.fail(ErrorResponse.of("UNAUTHORIZED", "로그인이 필요합니다.")));
            return;
        }
        CurrentUser.set(su);
        try {
            chain.doFilter(request, response);
        } finally {
            CurrentUser.clear();
        }
    }
}
