package com.grovesoft.boctracker.milestone.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "단계 내 순서 변경 요청. ids 는 해당 단계의 모든 마일스톤 id 를 새 순서대로 나열해야 한다.")
public record MilestoneOrderRequest(
        @Schema(description = "단계명", example = "P0. 수급·준비") @NotBlank @Size(max = 100) String phase,
        @Schema(description = "새 순서의 마일스톤 id 목록", example = "[3, 1, 2]") @NotEmpty List<Long> ids) {
}
