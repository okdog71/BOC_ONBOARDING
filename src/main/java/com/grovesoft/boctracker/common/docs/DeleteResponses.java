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

/** 삭제 공통 응답: 204(본문 없음) / 404. */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 성공 (본문 없음)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                description = "대상이 존재하지 않음 (NOT_FOUND)",
                content = @Content(schema = @Schema(implementation = ApiResponse.class)))
})
public @interface DeleteResponses {
}
