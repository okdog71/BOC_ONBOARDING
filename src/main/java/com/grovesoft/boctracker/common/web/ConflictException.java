package com.grovesoft.boctracker.common.web;

/** 현재 상태와 충돌하는 요청(예: 다른 항목이 참조 중인 항목 삭제). HTTP 409, code=CONFLICT. */
public class ConflictException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ConflictException(String message) {
        super(message);
    }
}
