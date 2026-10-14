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

/** 목록 조회 공통 응답: 200 / 400(필터·페이징 파라미터 형식 오류). */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                description = "요청 파라미터 형식 오류 (BAD_REQUEST)",
                content = @Content(schema = @Schema(implementation = ApiResponse.class)))
})
public @interface ListResponses {
}
