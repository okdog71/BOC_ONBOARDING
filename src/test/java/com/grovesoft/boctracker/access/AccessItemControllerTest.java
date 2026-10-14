package com.grovesoft.boctracker.access;

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
class AccessItemControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    private Map<String, Object> body() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("category", "저장소");
        m.put("target", "Git 저장소");
        m.put("usage", "소스 열람");
        m.put("envScope", "dev");
        m.put("procedure", "사내 포털 신청");
        m.put("requestedDate", "2026-11-02");
        m.put("owner", "인프라 담당");
        return m;
    }

    private long create(Map<String, Object> body) throws Exception {
        String res = mvc.perform(post("/api/access-items").contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return om.readTree(res).at("/data/id").asLong();
    }

    @Test
    void 생성_조회_목록_필터_happy_path() throws Exception {
        long id = create(body());
        Map<String, Object> other = body();
        other.put("category", "계정");
        other.put("status", "승인");
        create(other);

        mvc.perform(get("/api/access-items/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.category").value("저장소"))
                .andExpect(jsonPath("$.data.target").value("Git 저장소"))
                .andExpect(jsonPath("$.data.usage").value("소스 열람"))
                .andExpect(jsonPath("$.data.envScope").value("dev"))
                .andExpect(jsonPath("$.data.procedure").value("사내 포털 신청"))
                .andExpect(jsonPath("$.data.requestedDate").value("2026-11-02"))
                .andExpect(jsonPath("$.data.owner").value("인프라 담당"))
                .andExpect(jsonPath("$.data.status").value("미신청"));

        mvc.perform(get("/api/access-items").param("status", "미신청"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value(id));
        mvc.perform(get("/api/access-items").param("category", "저장소"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value(id));
        mvc.perform(get("/api/access-items"))
                .andExpect(jsonPath("$.data.totalElements").value(2));
        mvc.perform(get("/api/access-items").param("status", "없는값"))
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    void 수정_상태변경_삭제() throws Exception {
        long id = create(body());
        Map<String, Object> updated = body();
        updated.put("category", "수정된 값");
        mvc.perform(put("/api/access-items/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.category").value("수정된 값"));

        mvc.perform(patch("/api/access-items/{id}/status", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"승인\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("승인"));

        mvc.perform(delete("/api/access-items/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/access-items/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void 검증실패_400() throws Exception {
        Map<String, Object> bad = body();
        bad.remove("category");
        bad.remove("target");
        bad.put("status", "없는값");
        mvc.perform(post("/api/access-items").contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.fieldErrors", hasSize(3)));

        mvc.perform(patch("/api/access-items/1/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"없는값\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 미존재_404() throws Exception {
        mvc.perform(get("/api/access-items/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }
}
