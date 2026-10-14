package com.grovesoft.boctracker.handover;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class HandoverSessionControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    private Map<String, Object> body(String week, String category) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("week", week);
        m.put("category", category);
        m.put("domain", "환경·배포");
        m.put("topic", "배포 파이프라인 설명");
        m.put("ktStage", "설명·병행");
        m.put("plannedDate", "2026-11-02");
        return m;
    }

    private long create(Map<String, Object> body) throws Exception {
        String res = mvc.perform(post("/api/handover-sessions").contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return om.readTree(res).at("/data/id").asLong();
    }

    @Test
    void 생성_조회_목록_happy_path() throws Exception {
        long id = create(body("W1", "공통"));
        create(body("W2", "BOC"));

        mvc.perform(get("/api/handover-sessions/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.domain").value("환경·배포"))
                .andExpect(jsonPath("$.data.ktStage").value("설명·병행"))
                .andExpect(jsonPath("$.data.plannedDate").value("2026-11-02"))
                .andExpect(jsonPath("$.data.status").value("미착수"));

        mvc.perform(get("/api/handover-sessions").param("week", "W1").param("category", "공통"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)));

        mvc.perform(get("/api/handover-sessions"))
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    void plannedDate_없이_생성_가능하고_수정_상태변경_삭제() throws Exception {
        Map<String, Object> b = body("W3", "KEC");
        b.remove("plannedDate");
        long id = create(b);

        mvc.perform(get("/api/handover-sessions/{id}", id))
                .andExpect(jsonPath("$.data.plannedDate").doesNotExist());

        b.put("status", "예정");
        b.put("plannedDate", "2026-11-09");
        mvc.perform(put("/api/handover-sessions/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(b)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("예정"));

        mvc.perform(patch("/api/handover-sessions/{id}/status", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"완료\"}"))
                .andExpect(jsonPath("$.data.status").value("완료"));

        mvc.perform(delete("/api/handover-sessions/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/handover-sessions/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void 검증실패_400_week_category_ktStage() throws Exception {
        Map<String, Object> bad = body("W9", "기타");
        bad.put("ktStage", "설명·없음");

        mvc.perform(post("/api/handover-sessions").contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.fieldErrors", hasSize(3)));
    }

    @Test
    void 잘못된_날짜형식_400() throws Exception {
        Map<String, Object> bad = body("W1", "공통");
        bad.put("plannedDate", "not-a-date");

        mvc.perform(post("/api/handover-sessions").contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));
    }

    @Test
    void 예정은_HandoverSession_상태로_허용되지만_존재하지_않는_id는_404() throws Exception {
        mvc.perform(patch("/api/handover-sessions/{id}/status", 999999).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"예정\"}"))
                .andExpect(status().isNotFound());
    }
}
