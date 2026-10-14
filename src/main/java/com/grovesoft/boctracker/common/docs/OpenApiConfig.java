package com.grovesoft.boctracker.common.docs;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI 상단 정보와 태그 표시 순서를 정의한다.
 * 태그 이름/설명의 단일 출처는 각 컨트롤러의 {@code @Tag(name = ApiTags.XXX, description = ...)} 이다.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "BOC 온보딩·인수인계 트래커 API",
                version = "v1",
                description = """
                        KRAFTON BOC/KEC 운영 인수인계·온보딩의 마일스톤·인수인계(KT)·캐치업·수급/접근/신뢰도·메뉴 분석·이슈큐·RACI·회고를 관리하는 REST API.

                        - 응답은 `ApiResponse { success, data, error }` 래퍼를 사용하며, 오류는 `error { code, message, fieldErrors }` 입니다.
                        - 통제 어휘(상태·분류 등) 허용값은 `GET /api/meta/enums` 로 조회할 수 있습니다.
                        - 목록은 `page`, `size`, `sort` 쿼리 파라미터로 페이징합니다 (기본 20건, id 오름차순)."""))
public class OpenApiConfig {

    /** 동일 이름 태그를 하나로 합치고(설명이 있는 쪽 우선) {@link ApiTags#ORDER} 순서로 정렬한다. */
    @Bean
    public OpenApiCustomizer tagOrderCustomizer() {
        return openApi -> {
            if (openApi.getTags() == null) {
                return;
            }
            Map<String, Tag> byName = new LinkedHashMap<>();
            for (Tag tag : openApi.getTags()) {
                byName.merge(tag.getName(), tag, (a, b) -> a.getDescription() != null ? a : b);
            }
            List<Tag> sorted = new ArrayList<>(byName.values());
            sorted.sort(Comparator.comparingInt(t -> {
                int i = ApiTags.ORDER.indexOf(t.getName());
                return i < 0 ? Integer.MAX_VALUE : i;
            }));
            openApi.setTags(sorted);
        };
    }
}
