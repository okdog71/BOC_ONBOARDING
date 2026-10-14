package com.grovesoft.boctracker.common.web;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

/** Spring Data Page 를 API 로 직접 노출하지 않기 위한 안정적인 페이징 응답. */
@Schema(description = "페이징 응답")
public record PageResponse<T>(
        @Schema(description = "현재 페이지 항목") List<T> content,
        @Schema(description = "현재 페이지 번호(0부터)", example = "0") int page,
        @Schema(description = "페이지 크기", example = "20") int size,
        @Schema(description = "전체 항목 수", example = "21") long totalElements,
        @Schema(description = "전체 페이지 수", example = "2") int totalPages) {

    public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
