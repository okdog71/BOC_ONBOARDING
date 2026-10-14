package com.grovesoft.boctracker.raci;

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
class RaciAssignmentControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    private Map<String, Object> body() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("domainGroup", "redeem/order");
        m.put("scope", "쿠폰·주문 도메인");
        m.put("complexity", "상");
        m.put("scale", "중");
        m.put("generation", "B");
        m.put("responsible", "담당자 A");
        m.put("backup", "담당자 B");
        m.put("reviewer", "리드");
        m.put("menuCount", 12);
        m.put("targetDate", "2026-12-15");
        return m;
    }

    private long create(Map<String, Object> body) throws Exception {
        String res = mvc.perform(post("/api/raci-assignments").contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return om.readTree(res).at("/data/id").asLong();
    }

    @Test
    void 생성_조회_목록_필터_happy_path() throws Exception {
        long id = create(body());
        Map<String, Object> other = body();
        other.put("status", "진행중");
        create(other);

        mvc.perform(get("/api/raci-assignments/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.domainGroup").value("redeem/order"))
                .andExpect(jsonPath("$.data.scope").value("쿠폰·주문 도메인"))
                .andExpect(jsonPath("$.data.complexity").value("상"))
                .andExpect(jsonPath("$.data.scale").value("중"))
                .andExpect(jsonPath("$.data.generation").value("B"))
                .andExpect(jsonPath("$.data.responsible").value("담당자 A"))
                .andExpect(jsonPath("$.data.backup").value("담당자 B"))
                .andExpect(jsonPath("$.data.reviewer").value("리드"))
                .andExpect(jsonPath("$.data.menuCount").value(12))
                .andExpect(jsonPath("$.data.targetDate").value("2026-12-15"))
                .andExpect(jsonPath("$.data.status").value("미착수"));

        mvc.perform(get("/api/raci-assignments").param("status", "미착수"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value(id));
        mvc.perform(get("/api/raci-assignments"))
                .andExpect(jsonPath("$.data.totalElements").value(2));
        mvc.perform(get("/api/raci-assignments").param("status", "없는값"))
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    void 수정_상태변경_삭제() throws Exception {
        long id = create(body());
        Map<String, Object> updated = body();
        updated.put("domainGroup", "수정된 값");
        mvc.perform(put("/api/raci-assignments/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.domainGroup").value("수정된 값"));

        mvc.perform(patch("/api/raci-assignments/{id}/status", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"진행중\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("진행중"));

        mvc.perform(delete("/api/raci-assignments/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/raci-assignments/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void 검증실패_400() throws Exception {
        Map<String, Object> bad = body();
        bad.remove("domainGroup");
        bad.put("complexity", "없는값");
        mvc.perform(post("/api/raci-assignments").contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.fieldErrors", hasSize(2)));

        mvc.perform(patch("/api/raci-assignments/1/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"없는값\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 미존재_404() throws Exception {
        mvc.perform(get("/api/raci-assignments/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }
}
