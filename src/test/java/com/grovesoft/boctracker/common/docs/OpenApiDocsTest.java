package com.grovesoft.boctracker.common.docs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** /v3/api-docs 가 정상이고 태깅 규칙(한글 태그 15개·순서, summary, 공통 응답코드)과 README 엔드포인트 표를 지키는지 검증. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiDocsTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    JsonNode doc;

    private JsonNode doc() throws Exception {
        if (doc == null) {
            String body = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
            doc = om.readTree(body);
        }
        return doc;
    }

    private record Op(String method, String path, JsonNode node) {
    }

    private List<Op> operations() throws Exception {
        List<Op> ops = new ArrayList<>();
        for (Iterator<Map.Entry<String, JsonNode>> p = doc().get("paths").fields(); p.hasNext(); ) {
            Map.Entry<String, JsonNode> path = p.next();
            for (Iterator<Map.Entry<String, JsonNode>> m = path.getValue().fields(); m.hasNext(); ) {
                Map.Entry<String, JsonNode> method = m.next();
                ops.add(new Op(method.getKey().toUpperCase(), path.getKey(), method.getValue()));
            }
        }
        return ops;
    }

    @Test
    void 제목과_버전() throws Exception {
        assertThat(doc().at("/info/title").asText()).isEqualTo("BOC 온보딩·인수인계 트래커 API");
        assertThat(doc().at("/info/version").asText()).isEqualTo("v1");
    }

    @Test
    void 태그_15개가_중복없이_정의된_순서로_설명과_함께_나온다() throws Exception {
        List<String> names = new ArrayList<>();
        for (JsonNode t : doc().get("tags")) {
            names.add(t.get("name").asText());
            assertThat(t.path("description").asText()).as("설명: " + t.get("name").asText()).isNotBlank();
        }
        assertThat(names).containsExactlyElementsOf(ApiTags.ORDER);
        assertThat(names).hasSize(15);
    }

    @Test
    void 모든_엔드포인트에_한글_summary와_정의된_태그가_있다() throws Exception {
        for (Op op : operations()) {
            assertThat(op.node().path("summary").asText()).as(op.method() + " " + op.path()).isNotBlank();
            assertThat(op.node().get("tags")).as(op.method() + " " + op.path()).hasSize(1);
            assertThat(ApiTags.ORDER).contains(op.node().get("tags").get(0).asText());
        }
    }

    @Test
    void 공통_응답코드_패턴이_문서에_반영된다() throws Exception {
        for (Op op : operations()) {
            if (op.path().equals("/api/summary") || op.path().equals("/api/meta/enums")) {
                assertThat(codes(op)).as(op.path()).containsExactly("200");
                continue;
            }
            boolean byId = op.path().contains("{id}");
            Set<String> expected = switch (op.method()) {
                case "GET" -> byId ? Set.of("200", "404") : Set.of("200", "400");
                case "POST" -> Set.of("201", "400", "409");
                case "PUT" -> Set.of("200", "400", "404", "409");
                case "PATCH" -> Set.of("200", "400", "404");
                case "DELETE" -> Set.of("204", "404");
                default -> throw new AssertionError(op.method());
            };
            assertThat(codes(op)).as(op.method() + " " + op.path()).containsExactlyInAnyOrderElementsOf(expected);
        }
    }

    private Set<String> codes(Op op) {
        Set<String> s = new TreeSet<>();
        op.node().get("responses").fieldNames().forEachRemaining(s::add);
        return s;
    }

    @Test
    void 엔드포인트가_README_표와_일치한다() throws Exception {
        Map<String, Boolean> domains = new LinkedHashMap<>();   // 리소스 → PATCH /status 보유
        for (String d : List.of("milestones", "handover-sessions", "catchup-actions", "supply-docs",
                "access-items", "menu-inventories", "issue-queues", "raci-assignments", "review-logs",
                "analysis-cards")) {
            domains.put(d, true);
        }
        for (String d : List.of("mega-processes", "interface-links", "doc-reliabilities")) {
            domains.put(d, false);
        }
        Set<String> expected = new TreeSet<>(List.of("GET /api/meta/enums", "GET /api/summary"));
        domains.forEach((d, hasStatus) -> {
            expected.addAll(List.of("GET /api/" + d, "POST /api/" + d, "GET /api/" + d + "/{id}",
                    "PUT /api/" + d + "/{id}", "DELETE /api/" + d + "/{id}"));
            if (hasStatus) {
                expected.add("PATCH /api/" + d + "/{id}/status");
            }
        });
        // 마일스톤 고도화 엔드포인트(단계/담당자/체크리스트)
        expected.addAll(List.of("GET /api/milestones/phases", "PATCH /api/milestones/phases",
                "GET /api/milestones/owners", "PUT /api/milestones/order", "PUT /api/milestones/{id}/check-items",
                "PATCH /api/milestones/{id}/check-items/{itemId}",
                "POST /api/milestones/{id}/check-items/generate"));
        Set<String> actual = new TreeSet<>();
        operations().forEach(op -> actual.add(op.method() + " " + op.path()));

        assertThat(actual).isEqualTo(expected);
        assertThat(actual).hasSize(84);
    }

    @Test
    void 통제어휘_필드_스키마에_예시값이_있다() throws Exception {
        JsonNode schemas = doc().at("/components/schemas");
        assertThat(schemas.at("/MilestoneResponse/properties/status/example").asText()).isEqualTo("미착수");
        assertThat(schemas.at("/HandoverSessionRequest/properties/ktStage/example").asText()).isEqualTo("설명·병행");
        assertThat(schemas.at("/MenuInventoryRequest/properties/priority/example").asText()).isEqualTo("상");
        assertThat(schemas.at("/IssueQueueStatusRequest/properties/status/example").asText()).isEqualTo("확인중");
    }

    @Test
    void swagger_ui_진입점() throws Exception {
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }
}
