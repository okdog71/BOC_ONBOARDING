package com.grovesoft.boctracker.supplydoc;

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
class SupplyDocControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    private Map<String, Object> body() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("category", "A. 기획·화면");
        m.put("name", "화면 스토리보드");
        m.put("expectedContent", "메뉴별 화면 정의");
        m.put("usagePurpose", "메뉴 분석 기준");
        m.put("existence", "있음");
        m.put("location", "Confluence/BOC/스토리보드");
        m.put("freshness", "혼재");
        m.put("owner", "기획 담당");
        return m;
    }

    private long create(Map<String, Object> body) throws Exception {
        String res = mvc.perform(post("/api/supply-docs").contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return om.readTree(res).at("/data/id").asLong();
    }

    @Test
    void 생성_조회_목록_필터_happy_path() throws Exception {
        long id = create(body());
        Map<String, Object> other = body();
        other.put("category", "B. 설계·기술");
        other.put("supplyStatus", "수령");
        create(other);

        mvc.perform(get("/api/supply-docs/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.category").value("A. 기획·화면"))
                .andExpect(jsonPath("$.data.name").value("화면 스토리보드"))
                .andExpect(jsonPath("$.data.expectedContent").value("메뉴별 화면 정의"))
                .andExpect(jsonPath("$.data.usagePurpose").value("메뉴 분석 기준"))
                .andExpect(jsonPath("$.data.existence").value("있음"))
                .andExpect(jsonPath("$.data.location").value("Confluence/BOC/스토리보드"))
                .andExpect(jsonPath("$.data.freshness").value("혼재"))
                .andExpect(jsonPath("$.data.owner").value("기획 담당"))
                .andExpect(jsonPath("$.data.supplyStatus").value("미요청"));

        mvc.perform(get("/api/supply-docs").param("supplyStatus", "미요청"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value(id));
        mvc.perform(get("/api/supply-docs").param("category", "A. 기획·화면"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value(id));
        mvc.perform(get("/api/supply-docs"))
                .andExpect(jsonPath("$.data.totalElements").value(2));
        mvc.perform(get("/api/supply-docs").param("supplyStatus", "없는값"))
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    void 수정_상태변경_삭제() throws Exception {
        long id = create(body());
        Map<String, Object> updated = body();
        updated.put("category", "수정된 값");
        mvc.perform(put("/api/supply-docs/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.category").value("수정된 값"));

        mvc.perform(patch("/api/supply-docs/{id}/status", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"수령\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.supplyStatus").value("수령"));

        mvc.perform(delete("/api/supply-docs/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/supply-docs/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void 검증실패_400() throws Exception {
        Map<String, Object> bad = body();
        bad.remove("category");
        bad.remove("name");
        bad.put("existence", "없는값");
        mvc.perform(post("/api/supply-docs").contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.fieldErrors", hasSize(3)));

        mvc.perform(patch("/api/supply-docs/1/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"없는값\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 미존재_404() throws Exception {
        mvc.perform(get("/api/supply-docs/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }
}
