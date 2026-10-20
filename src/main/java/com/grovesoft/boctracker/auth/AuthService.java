package com.grovesoft.boctracker.auth;

import com.grovesoft.boctracker.common.web.BusinessRuleException;
import com.grovesoft.boctracker.user.AppUser;
import com.grovesoft.boctracker.user.UserRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_FAILURES = 5;
    private static final Duration LOCK = Duration.ofMinutes(1);
    /** 존재하지 않는 ID 에도 같은 비용의 해시 비교를 하기 위한 더미 해시(사용자 열거 방지). */
    private static final String DUMMY_HASH = "$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5BUfMfOUHx7N5uJ8mJrYy6y0yZyuS";

    private final UserRepository users;
    private final PasswordEncoder encoder;

    /** 로그인 ID 별 연속 실패(메모리). 5회 실패하면 1분간 시도를 막는다. */
    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    private static final class Attempt {
        int failures;
        Instant lockedUntil;
    }

    @Transactional(readOnly = true)
    public AppUser authenticate(String loginId, String password) {
        String key = loginId.trim();
        Attempt a = attempts.computeIfAbsent(key, k -> new Attempt());
        synchronized (a) {
            if (a.lockedUntil != null && Instant.now().isBefore(a.lockedUntil)) {
                throw new AuthException(HttpStatus.TOO_MANY_REQUESTS, "TOO_MANY_ATTEMPTS",
                        "로그인 실패가 반복되어 잠시 막혔습니다. 1분 뒤 다시 시도하세요.");
            }
            AppUser u = users.findByLoginId(key).orElse(null);
            boolean ok = encoder.matches(password, u == null ? DUMMY_HASH : u.getPasswordHash()) && u != null;
            if (!ok) {
                if (++a.failures >= MAX_FAILURES) {
                    a.lockedUntil = Instant.now().plus(LOCK);
                    a.failures = 0;
                }
                throw new AuthException(HttpStatus.UNAUTHORIZED, "AUTH_FAILED", "ID 또는 비밀번호가 올바르지 않습니다.");
            }
            a.failures = 0;
            a.lockedUntil = null;
            return u;
        }
    }

    @Transactional
    public AppUser changePassword(Long userId, String current, String next) {
        AppUser u = users.findById(userId)
                .orElseThrow(() -> new AuthException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "로그인이 필요합니다."));
        if (!encoder.matches(current, u.getPasswordHash())) {
            throw new BusinessRuleException("현재 비밀번호가 올바르지 않습니다.");
        }
        if (next.equals(u.getLoginId())) {
            throw new BusinessRuleException("새 비밀번호는 로그인 ID 와 달라야 합니다.");
        }
        u.changePassword(encoder.encode(next));
        return u;
    }

    @Transactional(readOnly = true)
    public AppUser find(Long userId) {
        return users.findById(userId).orElse(null);
    }
}
