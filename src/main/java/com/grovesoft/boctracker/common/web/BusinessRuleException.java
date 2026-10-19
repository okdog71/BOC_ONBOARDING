package com.grovesoft.boctracker.common.web;

/** 입력 형식은 맞지만 도메인 규칙을 위반한 요청(예: 순환 선행). HTTP 400, code=BUSINESS_RULE. */
public class BusinessRuleException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BusinessRuleException(String message) {
        super(message);
    }
}
