package com.grovesoft.boctracker.docreliability;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class DocReliabilityControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    private Map<String, Object> body() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("docName", "IA 정의서");
        m.put("source", "Confluence");
        m.put("lastUpdated", "2026-09");
        m.put("freshness", "현행");
        m.put("codeFit", "높음");
        m.put("grade", "A");
        m.put("verifyMethod", "코드 대조");
        m.put("gap", "신규 메뉴 누락");
        return m;
    }

    private long create(Map<String, Object> body) throws Exception {
        String res = mvc.perform(post("/api/doc-reliabilities").contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return om.readTree(res).at("/data/id").asLong();
    }

    @Test
    void 생성_조회_목록_필터_happy_path() throws Exception {
        long id = create(body());
        Map<String, Object> other = body();
        other.put("grade", "B");
        other.put("freshness", "레거시");
        create(other);

        mvc.perform(get("/api/doc-reliabilities/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.docName").value("IA 정의서"))
                .andExpect(jsonPath("$.data.source").value("Confluence"))
                .andExpect(jsonPath("$.data.lastUpdated").value("2026-09"))
                .andExpect(jsonPath("$.data.freshness").value("현행"))
                .andExpect(jsonPath("$.data.codeFit").value("높음"))
                .andExpect(jsonPath("$.data.grade").value("A"))
                .andExpect(jsonPath("$.data.verifyMethod").value("코드 대조"))
                .andExpect(jsonPath("$.data.gap").value("신규 메뉴 누락"));

        mvc.perform(get("/api/doc-reliabilities").param("grade", "A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value(id));
        mvc.perform(get("/api/doc-reliabilities").param("freshness", "현행"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value(id));
        mvc.perform(get("/api/doc-reliabilities"))
                .andExpect(jsonPath("$.data.totalElements").value(2));
        mvc.perform(get("/api/doc-reliabilities").param("grade", "없는값"))
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    void 수정_삭제() throws Exception {
        long id = create(body());
        Map<String, Object> updated = body();
        updated.put("docName", "수정된 값");
        mvc.perform(put("/api/doc-reliabilities/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.docName").value("수정된 값"));

        mvc.perform(delete("/api/doc-reliabilities/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/doc-reliabilities/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void 검증실패_400() throws Exception {
        Map<String, Object> bad = body();
        bad.remove("docName");
        bad.put("freshness", "없는값");
        mvc.perform(post("/api/doc-reliabilities").contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.fieldErrors", hasSize(2)));
    }

    @Test
    void 미존재_404() throws Exception {
        mvc.perform(get("/api/doc-reliabilities/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }
}
