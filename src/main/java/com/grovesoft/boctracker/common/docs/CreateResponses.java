package com.grovesoft.boctracker.common.docs;

import com.grovesoft.boctracker.common.web.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 생성 공통 응답: 201 / 400(검증 실패) / 409(중복·제약 위반). */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                description = "입력값 검증 실패 (VALIDATION_ERROR) 또는 요청 형식 오류 (BAD_REQUEST)",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409",
                description = "중복되었거나 제약조건을 위반 (CONFLICT)",
                content = @Content(schema = @Schema(implementation = ApiResponse.class)))
})
public @interface CreateResponses {
}
