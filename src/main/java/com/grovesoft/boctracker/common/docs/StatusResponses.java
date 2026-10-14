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

/** 상태 변경 공통 응답: 200 / 400(허용되지 않은 상태값) / 404. */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상태 변경 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                description = "허용되지 않은 상태값 (VALIDATION_ERROR) 또는 요청 형식 오류 (BAD_REQUEST)",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                description = "대상이 존재하지 않음 (NOT_FOUND)",
                content = @Content(schema = @Schema(implementation = ApiResponse.class)))
})
public @interface StatusResponses {
}
