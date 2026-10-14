package com.grovesoft.boctracker.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 통제 어휘(controlled vocabulary) 필드 검증. 애너테이션 속성은 컴파일 타임 상수만 허용하므로
 * 허용값은 리터럴로 적는다(값은 {@code common.meta.CodeValues} 및 CLAUDE.md 코드값 표와 동일하게 유지).
 */
@Documented
@Constraint(validatedBy = EnumValueValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumValue {

    String[] allowed();

    /** true면 null/blank 허용(기본값 세팅은 서비스/엔티티 책임). */
    boolean nullable() default true;

    /** 복합 표기 허용 시 구분자(예: "·" → "설명·병행"). 빈 문자열이면 단일값만 허용. */
    String delimiter() default "";

    String message() default "허용되지 않은 값입니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
