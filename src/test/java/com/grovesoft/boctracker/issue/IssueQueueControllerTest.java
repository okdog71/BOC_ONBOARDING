package com.grovesoft.boctracker.issue;

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
class IssueQueueControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    private Map<String, Object> body() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type", "이슈");
        m.put("item", "KEC-01");
        m.put("content", "쿠폰 중복 등록 허용 여부 불명확");
        m.put("impact", "정산 오류 가능");
        m.put("basis", "dev 화면 확인");
        m.put("confirmWith", "현업 담당자");
        m.put("owner", "BOC 리드");
        m.put("targetDate", "2026-11-30");
        m.put("action", "현업 인터뷰");
        return m;
    }

    private long create(Map<String, Object> body) throws Exception {
        String res = mvc.perform(post("/api/issue-queues").contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return om.readTree(res).at("/data/id").asLong();
    }

    @Test
    void 생성_조회_목록_필터_happy_path() throws Exception {
        long id = create(body());
        Map<String, Object> other = body();
        other.put("type", "미확인");
        other.put("status", "확인중");
        create(other);

        mvc.perform(get("/api/issue-queues/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.type").value("이슈"))
                .andExpect(jsonPath("$.data.item").value("KEC-01"))
                .andExpect(jsonPath("$.data.content").value("쿠폰 중복 등록 허용 여부 불명확"))
                .andExpect(jsonPath("$.data.impact").value("정산 오류 가능"))
                .andExpect(jsonPath("$.data.basis").value("dev 화면 확인"))
                .andExpect(jsonPath("$.data.confirmWith").value("현업 담당자"))
                .andExpect(jsonPath("$.data.owner").value("BOC 리드"))
                .andExpect(jsonPath("$.data.targetDate").value("2026-11-30"))
                .andExpect(jsonPath("$.data.action").value("현업 인터뷰"))
                .andExpect(jsonPath("$.data.status").value("미착수"));

        mvc.perform(get("/api/issue-queues").param("status", "미착수"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value(id));
        mvc.perform(get("/api/issue-queues").param("type", "이슈"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value(id));
        mvc.perform(get("/api/issue-queues"))
                .andExpect(jsonPath("$.data.totalElements").value(2));
        mvc.perform(get("/api/issue-queues").param("status", "없는값"))
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    void 수정_상태변경_삭제() throws Exception {
        long id = create(body());
        Map<String, Object> updated = body();
        updated.put("item", "수정된 값");
        mvc.perform(put("/api/issue-queues/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.item").value("수정된 값"));

        mvc.perform(patch("/api/issue-queues/{id}/status", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"확인중\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("확인중"));

        mvc.perform(delete("/api/issue-queues/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/issue-queues/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void 검증실패_400() throws Exception {
        Map<String, Object> bad = body();
        bad.remove("item");
        bad.put("type", "없는값");
        mvc.perform(post("/api/issue-queues").contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.fieldErrors", hasSize(2)));

        mvc.perform(patch("/api/issue-queues/1/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"없는값\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 미존재_404() throws Exception {
        mvc.perform(get("/api/issue-queues/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }
}
