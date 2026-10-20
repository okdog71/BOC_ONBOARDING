package com.grovesoft.boctracker.auth;

import java.io.Serializable;

/** 세션에 보관하는 로그인 사용자(비밀번호 등 민감정보 없음). */
public record SessionUser(Long id, String loginId, String name) implements Serializable {
}
