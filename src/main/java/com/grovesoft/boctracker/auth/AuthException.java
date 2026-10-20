package com.grovesoft.boctracker.auth;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** 인증 관련 오류(401 로그인 필요·실패, 429 시도 과다). */
@Getter
public class AuthException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public AuthException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }
}
