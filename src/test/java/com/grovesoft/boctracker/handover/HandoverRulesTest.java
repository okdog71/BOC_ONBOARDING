package com.grovesoft.boctracker.handover;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.grovesoft.boctracker.support.ApiTestSupport;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** ⑤ 인수인계: 코드·순서, 완료일/지연, 마일스톤·수급·접근 FK, 시작 전 미충족(blockers), 삭제 보호. */
class HandoverRulesTest extends ApiTestSupport {

    private static final String API = "/api/handover-sessions";

    private Map<String, Object> session(String week, String topic) {
        return map("week", week, "category", "공통", "topic", topic);
    }

    private JsonNode milestone(String phase, String name, String output) throws Exception {
        return create("/api/milestones", map("phase", phase, "name", name, "output", output));
    }

    @Test
    void 코드는_주차_순번이고_주차별_No가_따로_매겨진다() throws Exception {
        JsonNode a = create(API, session("W1", "a"));
        JsonNode b = create(API, session("W2", "b"));
        JsonNode c = create(API, session("W1", "c"));
        assertThat(List.of(a.get("code").asText(), b.get("code").asText(), c.get("code").asText()))
                .containsExactly("W1.1", "W2.1", "W1.2");
        assertThat(c.get("no").asInt()).isEqualTo(2);

        send(put(API + "/order"), map("group", "W1", "ids", List.of(c.get("id").asLong(), a.get("id").asLong())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data[0].code").value("W1.2"));
    }

    @Test
    void 주차를_옮기면_새_주차_맨뒤_코드_유지_옛_주차는_번호_재정렬() throws Exception {
        long a = create(API, session("W1", "a")).get("id").asLong();
        long b = create(API, session("W1", "b")).get("id").asLong();
        send(put(API + "/{id}", a), session("W2", "a")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("W1.1"))
                .andExpect(jsonPath("$.data.no").value(1));
        assertThat(fetch(API, b).get("no").asInt()).isEqualTo(1);
    }

    @Test
    void 번호가_큰_항목을_옮겨도_새_그룹_번호는_빈틈이_없다() throws Exception {
        create(API, session("W1", "a"));
        create(API, session("W1", "b"));
        long c = create(API, session("W1", "c")).get("id").asLong();   // W1 의 No.3
        long x = create(API, session("W2", "x")).get("id").asLong();   // W2 의 No.1

        send(put(API + "/{id}", c), session("W2", "c")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.no").value(2));   // 4 가 아니라 2
        assertThat(fetch(API, x).get("no").asInt()).isEqualTo(1);
    }

    @Test
    void 완료일_자동기록과_지연() throws Exception {
        Map<String, Object> body = session("W1", "a");
        body.put("plannedDate", LocalDate.now().minusDays(2).toString());
        body.put("status", "예정");
        long id = create(API, body).get("id").asLong();
        mvc.perform(get(API + "/{id}", id)).andExpect(jsonPath("$.data.overdue").value(true));
        mvc.perform(get(API).param("overdue", "true")).andExpect(jsonPath("$.data.content", hasSize(1)));

        setStatus(API, id, "완료").andExpect(jsonPath("$.data.completedDate").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.data.overdue").value(false));
        setStatus(API, id, "진행중").andExpect(jsonPath("$.data.completedDate").doesNotExist());
    }

    @Test
    void 마일스톤_수급_접근이_FK로_연결되고_미충족은_blockers로_보인다() throws Exception {
        JsonNode ms = milestone("HO. 인수인계", "Week1 KT", "인수인계 일정(⑤)");
        long doc = create("/api/supply-docs", map("category", "B. 설계", "name", "배포 구조")).get("id").asLong();
        long acc = create("/api/access-items", map("category", "인프라", "target", "ArgoCD")).get("id").asLong();

        Map<String, Object> body = session("W1", "환경·배포");
        body.put("milestoneCode", ms.get("code").asText());
        body.put("supplyDocCodes", List.of("B.1"));
        body.put("accessItemCodes", List.of("AC.1"));
        long id = create(API, body).get("id").asLong();

        mvc.perform(get(API + "/{id}", id))
                .andExpect(jsonPath("$.data.milestoneCode").value("HO.1"))
                .andExpect(jsonPath("$.data.supplyDocs[0].code").value("B.1"))
                .andExpect(jsonPath("$.data.accessItems[0].code").value("AC.1"))
                .andExpect(jsonPath("$.data.blockers", hasSize(2)));

        // 반대 방향 링크: 수급·접근 쪽에서 이 세션이 보인다
        mvc.perform(get("/api/supply-docs/{id}", doc)).andExpect(jsonPath("$.data.requiredBySessions[0]").value("W1.1"));
        mvc.perform(get("/api/access-items/{id}", acc)).andExpect(jsonPath("$.data.requiredBySessions[0]").value("W1.1"));

        // 충족되면 blockers 가 사라진다
        setStatus("/api/supply-docs", doc, "수령");
        setStatus("/api/access-items", acc, "승인");
        mvc.perform(get(API + "/{id}", id)).andExpect(jsonPath("$.data.blockers", empty()));
    }

    @Test
    void 존재하지_않는_코드로_연결하면_400() throws Exception {
        Map<String, Object> bad = session("W1", "a");
        bad.put("milestoneCode", "ZZ.9");
        send(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(API), bad)
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.message", containsString("ZZ.9")));
        Map<String, Object> bad2 = session("W1", "a");
        bad2.put("supplyDocCodes", List.of("Q.1"));
        send(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(API), bad2)
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.message", containsString("Q.1")));
    }

    @Test
    void 참조_중인_수급_접근_마일스톤은_삭제할_수_없다() throws Exception {
        JsonNode ms = milestone("HO. 인수인계", "Week1 KT", "인수인계 일정(⑤)");
        long doc = create("/api/supply-docs", map("category", "B. 설계", "name", "배포 구조")).get("id").asLong();
        long acc = create("/api/access-items", map("category", "인프라", "target", "ArgoCD")).get("id").asLong();
        Map<String, Object> body = session("W1", "x");
        body.put("milestoneCode", ms.get("code").asText());
        body.put("supplyDocCodes", List.of("B.1"));
        body.put("accessItemCodes", List.of("AC.1"));
        long sid = create(API, body).get("id").asLong();

        mvc.perform(delete("/api/supply-docs/{id}", doc)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.message", containsString("W1.1")));
        mvc.perform(delete("/api/access-items/{id}", acc)).andExpect(status().isConflict());
        mvc.perform(delete("/api/milestones/{id}", ms.get("id").asLong())).andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.message", containsString("W1.1")));

        mvc.perform(delete(API + "/{id}", sid)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/supply-docs/{id}", doc)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/access-items/{id}", acc)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/milestones/{id}", ms.get("id").asLong())).andExpect(status().isNoContent());
    }

    @Test
    void 마무리_세션은_다른_세션이_끝나야_한다() throws Exception {
        long first = create(API, session("W1", "first")).get("id").asLong();
        Map<String, Object> last = session("W4", "cut-over");
        last.put("precondition", "전 세션 완료");
        long lastId = create(API, last).get("id").asLong();

        mvc.perform(get(API + "/{id}", lastId)).andExpect(jsonPath("$.data.blockers[0]", containsString("W1.1")));
        setStatus(API, first, "완료");
        mvc.perform(get(API + "/{id}", lastId)).andExpect(jsonPath("$.data.blockers", empty()));
    }

    @Test
    void 연결_해제와_전체_교체() throws Exception {
        create("/api/supply-docs", map("category", "B. 설계", "name", "배포 구조"));
        Map<String, Object> body = session("W1", "x");
        body.put("supplyDocCodes", List.of("B.1"));
        long id = create(API, body).get("id").asLong();
        assertThat(fetch(API, id).get("supplyDocs")).hasSize(1);

        // PUT 은 전체 교체 — 목록을 생략하면 연결이 해제된다
        Map<String, Object> update = new LinkedHashMap<>(session("W1", "x"));
        send(put(API + "/{id}", id), update).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.supplyDocs", empty()));
    }
}
