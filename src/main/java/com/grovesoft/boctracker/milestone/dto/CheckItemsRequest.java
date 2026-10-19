package com.grovesoft.boctracker.milestone.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "체크리스트 전체 교체 요청. 목록 순서가 정렬 순서이며, 목록에 없는 기존 항목은 삭제된다.")
public record CheckItemsRequest(
        @Valid @NotNull @Size(max = 50) List<Item> items) {

    @Schema(description = "체크리스트 항목")
    public record Item(
            @Schema(description = "기존 항목 ID(신규는 생략)", example = "10") Long id,
            @Schema(description = "내용", example = "Jira 접근 확인") @NotBlank @Size(max = 500) String title,
            @Schema(description = "완료 여부", example = "false") boolean done) {
    }
}
