package com.grovesoft.boctracker.milestone;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/** 마일스톤 비즈니스 규칙: 완료일, 지연, 선행(순환/삭제/코드변경 보호), 체크리스트, 단계. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MilestoneRulesTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    private Map<String, Object> body(String code, String phase) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("phase", phase);
        m.put("code", code);
        m.put("name", code + " 이름");
        return m;
    }

    private long create(Map<String, Object> body) throws Exception {
        String res = mvc.perform(post("/api/milestones").contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return om.readTree(res).at("/data/id").asLong();
    }

    private ResultActions send(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder b,
                               Object json) throws Exception {
        return mvc.perform(b.contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(json)));
    }

    private ResultActions setStatus(long id, String status) throws Exception {
        return send(patch("/api/milestones/{id}/status", id), Map.of("status", status));
    }

    @Test
    void 완료_전환시_완료일_기록_이탈시_제거() throws Exception {
        long id = create(body("R-1", "P"));
        mvc.perform(get("/api/milestones/{id}", id)).andExpect(jsonPath("$.data.completedDate").value(nullValue()));

        setStatus(id, "완료").andExpect(status().isOk())
                .andExpect(jsonPath("$.data.completedDate").value(LocalDate.now().toString()));
        setStatus(id, "진행중").andExpect(jsonPath("$.data.completedDate").value(nullValue()));
    }

    @Test
    void 지연은_목표일이_지났고_완료가_아닐때만() throws Exception {
        Map<String, Object> b = body("R-2", "P");
        b.put("targetDate", LocalDate.now().minusDays(3).toString());
        long id = create(b);

        mvc.perform(get("/api/milestones/{id}", id)).andExpect(jsonPath("$.data.overdue").value(true));
        mvc.perform(get("/api/milestones?overdue=true")).andExpect(jsonPath("$.data.content", hasSize(1)));

        setStatus(id, "완료").andExpect(jsonPath("$.data.overdue").value(false));
        mvc.perform(get("/api/milestones?overdue=true")).andExpect(jsonPath("$.data.content", hasSize(0)));
    }

    @Test
    void 선행이_미완료면_blockedBy에_표시() throws Exception {
        long a = create(body("B-1", "P"));
        Map<String, Object> b = body("B-2", "P");
        b.put("precondition", "B-1");
        long bId = create(b);

        mvc.perform(get("/api/milestones/{id}", bId))
                .andExpect(jsonPath("$.data.predecessorCodes", contains("B-1")))
                .andExpect(jsonPath("$.data.blockedBy", contains("B-1")));

        setStatus(a, "완료");
        mvc.perform(get("/api/milestones/{id}", bId)).andExpect(jsonPath("$.data.blockedBy", empty()));
    }

    @Test
    void 순환_선행은_거부() throws Exception {
        long a = create(body("C-1", "P"));
        Map<String, Object> b = body("C-2", "P");
        b.put("precondition", "C-1");
        create(b);

        Map<String, Object> aUpdate = body("C-1", "P");
        aUpdate.put("precondition", "C-2");
        send(put("/api/milestones/{id}", a), aUpdate)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BUSINESS_RULE"))
                .andExpect(jsonPath("$.error.message", containsString("순환")));
    }

    private String codeOf(long id) throws Exception {
        String res = mvc.perform(get("/api/milestones/{id}", id)).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        return om.readTree(res).at("/data/code").asText();
    }

    private Map<String, Object> noCode(String phase, String name) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("phase", phase);
        m.put("name", name);
        return m;
    }

    @Test
    void 코드는_단계접두로_자동_부여되고_순번은_최대값_기준() throws Exception {
        long a = create(noCode("P0. 수급·준비", "a"));
        long b = create(noCode("P0. 수급·준비", "b"));
        long h = create(noCode("HO. 인수인계(KT)", "h"));
        long x = create(noCode("운영", "x"));
        org.assertj.core.api.Assertions.assertThat(List.of(codeOf(a), codeOf(b), codeOf(h), codeOf(x)))
                .containsExactly("P0.1", "P0.2", "HO.1", "X.1");

        // 중간 항목을 지워도 번호를 재사용하지 않고, 최대값 + 1
        mvc.perform(delete("/api/milestones/{id}", a)).andExpect(status().isNoContent());
        long c = create(noCode("P0. 수급·준비", "c"));
        org.assertj.core.api.Assertions.assertThat(codeOf(c)).isEqualTo("P0.3");
    }

    @Test
    void 새_항목은_단계_맨뒤_No_이고_삭제시_다시_촘촘히() throws Exception {
        long a = create(noCode("P0. 단계", "a"));
        long b = create(noCode("P0. 단계", "b"));
        long c = create(noCode("P0. 단계", "c"));
        mvc.perform(get("/api/milestones/{id}", c)).andExpect(jsonPath("$.data.no").value(3));

        mvc.perform(delete("/api/milestones/{id}", a)).andExpect(status().isNoContent());
        mvc.perform(get("/api/milestones/{id}", b)).andExpect(jsonPath("$.data.no").value(1));
        mvc.perform(get("/api/milestones/{id}", c)).andExpect(jsonPath("$.data.no").value(2));
    }

    @Test
    void 순서_변경은_No만_바꾸고_코드는_그대로() throws Exception {
        long a = create(noCode("P0. 단계", "a"));
        long b = create(noCode("P0. 단계", "b"));
        long c = create(noCode("P0. 단계", "c"));

        send(put("/api/milestones/order"), Map.of("phase", "P0. 단계", "ids", List.of(c, a, b)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").value("P0.3"))
                .andExpect(jsonPath("$.data[0].no").value(1))
                .andExpect(jsonPath("$.data[2].code").value("P0.2"));
        mvc.perform(get("/api/milestones/{id}", a)).andExpect(jsonPath("$.data.no").value(2))
                .andExpect(jsonPath("$.data.code").value("P0.1"));
    }

    @Test
    void 순서_변경_목록이_다르면_400_없는_단계는_404() throws Exception {
        long a = create(noCode("P0. 단계", "a"));
        create(noCode("P0. 단계", "b"));
        send(put("/api/milestones/order"), Map.of("phase", "P0. 단계", "ids", List.of(a)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.code").value("BUSINESS_RULE"));
        send(put("/api/milestones/order"), Map.of("phase", "없는단계", "ids", List.of(a)))
                .andExpect(status().isNotFound());
    }

    @Test
    void 단계를_옮기면_새_단계_맨뒤로_가고_코드는_유지() throws Exception {
        long a = create(noCode("P0. 단계", "a"));
        long b = create(noCode("P0. 단계", "b"));
        long x = create(noCode("P1. 다음", "x"));

        Map<String, Object> move = noCode("P1. 다음", "a");
        send(put("/api/milestones/{id}", a), move).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("P0.1"))
                .andExpect(jsonPath("$.data.no").value(2));
        mvc.perform(get("/api/milestones/{id}", b)).andExpect(jsonPath("$.data.no").value(1));
        mvc.perform(get("/api/milestones/{id}", x)).andExpect(jsonPath("$.data.no").value(1));
    }

    @Test
    void 번호가_큰_항목을_다른_단계로_옮겨도_번호에_빈틈이_없다() throws Exception {
        create(noCode("P0. 단계", "a"));
        create(noCode("P0. 단계", "b"));
        long c = create(noCode("P0. 단계", "c"));          // P0 의 No.3
        long x = create(noCode("P1. 다음", "x"));          // P1 의 No.1

        send(put("/api/milestones/{id}", c), noCode("P1. 다음", "c")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.no").value(2));   // 4 가 아니라 2
        mvc.perform(get("/api/milestones/{id}", x)).andExpect(jsonPath("$.data.no").value(1));
    }

    @Test
    void 범위_선행은_화면_순서_기준으로_해석() throws Exception {
        long a = create(noCode("P0. 단계", "a"));
        long b = create(noCode("P0. 단계", "b"));
        long c = create(noCode("P0. 단계", "c"));
        send(put("/api/milestones/order"), Map.of("phase", "P0. 단계", "ids", List.of(a, c, b)));

        Map<String, Object> d = noCode("P0. 단계", "d");
        d.put("precondition", "P0.1~P0.3");   // 화면 순서: P0.1, P0.3, P0.2 → 범위는 [P0.1, P0.3] 뿐
        long dId = create(d);
        mvc.perform(get("/api/milestones/{id}", dId))
                .andExpect(jsonPath("$.data.predecessorCodes", contains("P0.1", "P0.3")));
    }

    @Test
    void 선행으로_참조중이면_삭제는_409_코드변경은_400() throws Exception {
        long a = create(body("D-1", "P"));
        Map<String, Object> b = body("D-2", "P");
        b.put("precondition", "D-1");
        long bId = create(b);

        mvc.perform(delete("/api/milestones/{id}", a))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.message", containsString("D-2")));
        send(put("/api/milestones/{id}", a), body("D-1X", "P"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message", containsString("변경할 수 없습니다")));

        // 참조하는 쪽을 먼저 지우면 선행도 삭제 가능
        mvc.perform(delete("/api/milestones/{id}", bId)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/milestones/{id}", a)).andExpect(status().isNoContent());
    }

    @Test
    void 코드_중복은_409() throws Exception {
        create(body("E-1", "P"));
        send(post("/api/milestones"), body("E-1", "P")).andExpect(status().isConflict());
        long other = create(body("E-2", "P"));
        // 코드는 불변 — 다른 항목의 코드로도 바꿀 수 없다
        send(put("/api/milestones/{id}", other), body("E-1", "P")).andExpect(status().isBadRequest());
        // 코드를 보내지 않거나 같은 코드를 보내는 수정은 정상
        send(put("/api/milestones/{id}", other), body("E-2", "P")).andExpect(status().isOk());
        send(put("/api/milestones/{id}", other), noCode("P", "E-2 이름")).andExpect(status().isOk());
    }

    @Test
    void 전방_참조는_대상이_생성될때_연결() throws Exception {
        Map<String, Object> b = body("F-2", "P");
        b.put("precondition", "F-1");
        long bId = create(b);
        mvc.perform(get("/api/milestones/{id}", bId)).andExpect(jsonPath("$.data.predecessorCodes", empty()));

        create(body("F-1", "P"));
        mvc.perform(get("/api/milestones/{id}", bId)).andExpect(jsonPath("$.data.predecessorCodes", contains("F-1")));
    }

    @Test
    void 체크리스트_교체_토글_진척() throws Exception {
        long id = create(body("K-1", "P"));
        send(put("/api/milestones/{id}/check-items", id),
                Map.of("items", List.of(Map.of("title", "하나", "done", true), Map.of("title", "둘", "done", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.progress.done").value(1))
                .andExpect(jsonPath("$.data.progress.total").value(2))
                .andExpect(jsonPath("$.data.progress.percent").value(50));

        String res = mvc.perform(get("/api/milestones/{id}", id)).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        long second = om.readTree(res).at("/data/checkItems/1/id").asLong();

        send(patch("/api/milestones/{id}/check-items/{item}", id, second), Map.of("done", true))
                .andExpect(jsonPath("$.data.progress.percent").value(100));
        send(patch("/api/milestones/{id}/check-items/{item}", id, 999999), Map.of("done", true))
                .andExpect(status().isNotFound());

        // 둘째만 남기고 첫째 삭제, 새 항목 추가
        send(put("/api/milestones/{id}/check-items", id),
                Map.of("items", List.of(Map.of("id", second, "title", "둘", "done", true),
                        Map.of("title", "셋", "done", false))))
                .andExpect(jsonPath("$.data.checkItems", hasSize(2)))
                .andExpect(jsonPath("$.data.checkItems[0].title").value("둘"))
                .andExpect(jsonPath("$.data.checkItems[1].title").value("셋"));
    }

    @Test
    void 체크포인트로_체크리스트_생성() throws Exception {
        Map<String, Object> b = body("G-1", "P");
        b.put("checkpoint", "스토리보드·업무흐름도·IA");
        long id = create(b);

        mvc.perform(post("/api/milestones/{id}/check-items/generate", id))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.checkItems", hasSize(3)))
                .andExpect(jsonPath("$.data.progress.percent").value(0));
        mvc.perform(post("/api/milestones/{id}/check-items/generate", id)).andExpect(status().isConflict());

        long empty = create(body("G-2", "P"));
        mvc.perform(post("/api/milestones/{id}/check-items/generate", empty)).andExpect(status().isBadRequest());
    }

    @Test
    void 단계는_첫_등장순_정렬_이름변경_병합() throws Exception {
        create(body("H-1", "HO. 인수인계"));
        create(body("H-2", "P0. 준비"));
        create(body("H-3", "P0. 준비"));
        setStatus(create(body("H-4", "P1. IA")), "완료");

        mvc.perform(get("/api/milestones/phases"))
                .andExpect(jsonPath("$.data[0].phase").value("HO. 인수인계"))
                .andExpect(jsonPath("$.data[1].phase").value("P0. 준비"))
                .andExpect(jsonPath("$.data[1].total").value(2))
                .andExpect(jsonPath("$.data[2].done").value(1));

        send(patch("/api/milestones/phases"), Map.of("from", "P1. IA", "to", "P0. 준비"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[1].total").value(3));
        send(patch("/api/milestones/phases"), Map.of("from", "없는단계", "to", "X")).andExpect(status().isNotFound());
    }

    @Test
    void 담당_검색_필터와_담당자_목록() throws Exception {
        Map<String, Object> b = body("O-1", "P");
        b.put("owner", "홍길동");
        create(b);
        create(body("O-2", "P"));

        mvc.perform(get("/api/milestones/owners")).andExpect(jsonPath("$.data", contains("홍길동")));
        mvc.perform(get("/api/milestones").param("owner", "홍길동"))
                .andExpect(jsonPath("$.data.content", hasSize(1)));
        mvc.perform(get("/api/milestones").param("q", "o-2"))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].code").value("O-2"));
    }
}
