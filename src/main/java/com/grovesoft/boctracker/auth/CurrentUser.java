package com.grovesoft.boctracker.auth;

import java.util.Optional;

/** 현재 요청의 로그인 사용자. AuthFilter 가 설정하고 요청이 끝나면 지운다(감사 컬럼의 등록자·수정자 출처). */
public final class CurrentUser {

    /** 로그인 사용자가 없는 작업(시드 적재·기동 시 초기화 등)의 감사 값. */
    public static final String SYSTEM = "system";

    private static final ThreadLocal<SessionUser> HOLDER = new ThreadLocal<>();

    private CurrentUser() {
    }

    public static void set(SessionUser user) {
        HOLDER.set(user);
    }

    public static void clear() {
        HOLDER.remove();
    }

    public static Optional<SessionUser> get() {
        return Optional.ofNullable(HOLDER.get());
    }

    /** 감사 컬럼에 기록할 값: 로그인 ID, 없으면 "system". */
    public static String actor() {
        SessionUser u = HOLDER.get();
        return u == null ? SYSTEM : u.loginId();
    }
}
