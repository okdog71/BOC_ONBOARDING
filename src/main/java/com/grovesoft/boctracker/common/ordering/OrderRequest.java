package com.grovesoft.boctracker.common.ordering;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "그룹 내 순서 변경 요청. ids 는 해당 그룹의 모든 id 를 새 순서대로 나열해야 한다.")
public record OrderRequest(
        @Schema(description = "그룹명(구분/주차 등)", example = "A. 기획·화면") @NotBlank @Size(max = 100) String group,
        @Schema(description = "새 순서의 id 목록", example = "[3, 1, 2]") @NotEmpty List<Long> ids) {
}
