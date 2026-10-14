package com.grovesoft.boctracker.review;

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
class ReviewLogControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    private Map<String, Object> body() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("round", 1);
        m.put("reviewDate", "2026-11-10");
        m.put("targetDomain", "redeem");
        m.put("presenter", "담당자 A");
        m.put("attendees", "리드, 담당자 B");
        m.put("coveragePct", 80);
        m.put("crossCheck", "스토리보드 대조");
        m.put("gaps", "예외 케이스 누락");
        m.put("devUnconfirmed", "mock 응답 확인 필요");
        m.put("action", "보완 분석");
        m.put("actionOwner", "담당자 A");
        m.put("targetDate", "2026-11-17");
        return m;
    }

    private long create(Map<String, Object> body) throws Exception {
        String res = mvc.perform(post("/api/review-logs").contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return om.readTree(res).at("/data/id").asLong();
    }

    @Test
    void 생성_조회_목록_필터_happy_path() throws Exception {
        long id = create(body());
        Map<String, Object> other = body();
        other.put("status", "완료");
        create(other);

        mvc.perform(get("/api/review-logs/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.round").value(1))
                .andExpect(jsonPath("$.data.reviewDate").value("2026-11-10"))
                .andExpect(jsonPath("$.data.targetDomain").value("redeem"))
                .andExpect(jsonPath("$.data.presenter").value("담당자 A"))
                .andExpect(jsonPath("$.data.attendees").value("리드, 담당자 B"))
                .andExpect(jsonPath("$.data.coveragePct").value(80))
                .andExpect(jsonPath("$.data.crossCheck").value("스토리보드 대조"))
                .andExpect(jsonPath("$.data.gaps").value("예외 케이스 누락"))
                .andExpect(jsonPath("$.data.devUnconfirmed").value("mock 응답 확인 필요"))
                .andExpect(jsonPath("$.data.action").value("보완 분석"))
                .andExpect(jsonPath("$.data.actionOwner").value("담당자 A"))
                .andExpect(jsonPath("$.data.targetDate").value("2026-11-17"))
                .andExpect(jsonPath("$.data.status").value("미착수"));

        mvc.perform(get("/api/review-logs").param("status", "미착수"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value(id));
        mvc.perform(get("/api/review-logs"))
                .andExpect(jsonPath("$.data.totalElements").value(2));
        mvc.perform(get("/api/review-logs").param("status", "없는값"))
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    void 수정_상태변경_삭제() throws Exception {
        long id = create(body());
        Map<String, Object> updated = body();
        updated.put("round", 99);
        mvc.perform(put("/api/review-logs/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.round").value(99));

        mvc.perform(patch("/api/review-logs/{id}/status", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"완료\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("완료"));

        mvc.perform(delete("/api/review-logs/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/review-logs/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void 검증실패_400() throws Exception {
        Map<String, Object> bad = body();
        bad.remove("round");
        bad.put("status", "없는값");
        mvc.perform(post("/api/review-logs").contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.fieldErrors", hasSize(2)));

        mvc.perform(patch("/api/review-logs/1/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"없는값\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 미존재_404() throws Exception {
        mvc.perform(get("/api/review-logs/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }
}
