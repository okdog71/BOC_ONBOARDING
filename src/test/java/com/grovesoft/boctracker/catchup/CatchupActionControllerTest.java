package com.grovesoft.boctracker.catchup;

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
class CatchupActionControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    private Map<String, Object> body() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("category", "투입 전 활용");
        m.put("action", "스토리보드 사전 리뷰");
        m.put("purpose", "도메인 이해도 선확보");
        m.put("timing", "투입전");
        m.put("owner", "BOC 리드");
        m.put("kpi", "리뷰 완료율 100%");
        return m;
    }

    private long create(Map<String, Object> body) throws Exception {
        String res = mvc.perform(post("/api/catchup-actions").contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return om.readTree(res).at("/data/id").asLong();
    }

    @Test
    void 생성_조회_목록_필터_happy_path() throws Exception {
        long id = create(body());
        Map<String, Object> other = body();
        other.put("timing", "상시");
        other.put("status", "완료");
        create(other);

        mvc.perform(get("/api/catchup-actions/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.category").value("투입 전 활용"))
                .andExpect(jsonPath("$.data.action").value("스토리보드 사전 리뷰"))
                .andExpect(jsonPath("$.data.purpose").value("도메인 이해도 선확보"))
                .andExpect(jsonPath("$.data.timing").value("투입전"))
                .andExpect(jsonPath("$.data.owner").value("BOC 리드"))
                .andExpect(jsonPath("$.data.kpi").value("리뷰 완료율 100%"))
                .andExpect(jsonPath("$.data.status").value("미착수"));

        mvc.perform(get("/api/catchup-actions").param("status", "미착수"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value(id));
        mvc.perform(get("/api/catchup-actions").param("timing", "투입전"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value(id));
        mvc.perform(get("/api/catchup-actions"))
                .andExpect(jsonPath("$.data.totalElements").value(2));
        mvc.perform(get("/api/catchup-actions").param("status", "없는값"))
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    void 수정_상태변경_삭제() throws Exception {
        long id = create(body());
        Map<String, Object> updated = body();
        updated.put("category", "수정된 값");
        mvc.perform(put("/api/catchup-actions/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.category").value("수정된 값"));

        mvc.perform(patch("/api/catchup-actions/{id}/status", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"완료\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("완료"));

        mvc.perform(delete("/api/catchup-actions/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/catchup-actions/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void 검증실패_400() throws Exception {
        Map<String, Object> bad = body();
        bad.remove("category");
        bad.remove("action");
        bad.put("timing", "없는값");
        mvc.perform(post("/api/catchup-actions").contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.fieldErrors", hasSize(3)));

        mvc.perform(patch("/api/catchup-actions/1/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"없는값\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 미존재_404() throws Exception {
        mvc.perform(get("/api/catchup-actions/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }
}
