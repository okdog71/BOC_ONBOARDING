package com.grovesoft.boctracker.milestone;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class MilestoneControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    private Map<String, Object> body(String code) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("phase", "P0. 수급·준비");
        m.put("code", code);
        m.put("name", "수급 산출물 확인");
        m.put("goal", "기존 BOC 운영 산출물 확인");
        return m;
    }

    private long create(Map<String, Object> body) throws Exception {
        String res = mvc.perform(post("/api/milestones").contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        JsonNode node = om.readTree(res);
        return node.at("/data/id").asLong();
    }

    @Test
    void precondition_코드가_선행_FK로_해석된다() throws Exception {
        create(body("D-1"));
        create(body("D-2"));
        Map<String, Object> b = body("D-3");
        b.put("precondition", "D-1,D-2");
        long id = create(b);

        mvc.perform(get("/api/milestones/{id}", id))
                .andExpect(jsonPath("$.data.predecessorCodes", hasSize(2)))
                .andExpect(jsonPath("$.data.predecessorCodes[0]").value("D-1"));

        b.put("precondition", "현업 인터뷰");
        mvc.perform(put("/api/milestones/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(b)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.predecessorCodes", hasSize(0)))
                .andExpect(jsonPath("$.data.precondition").value("현업 인터뷰"));
    }

    @Test
    void 생성_조회_목록_happy_path() throws Exception {
        long id = create(body("C-1"));

        mvc.perform(get("/api/milestones/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("수급 산출물 확인"))
                .andExpect(jsonPath("$.data.status").value("미착수"));

        mvc.perform(get("/api/milestones").param("status", "미착수").param("phase", "P0. 수급·준비"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void 수정_상태변경_삭제() throws Exception {
        long id = create(body("C-2"));

        Map<String, Object> updated = body("C-2");
        updated.put("name", "접근권한 확보");
        updated.put("status", "진행중");
        mvc.perform(put("/api/milestones/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("접근권한 확보"))
                .andExpect(jsonPath("$.data.status").value("진행중"));

        mvc.perform(patch("/api/milestones/{id}/status", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"완료\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("완료"));

        mvc.perform(delete("/api/milestones/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/milestones/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void 검증실패_400_허용되지_않은_status와_필수값_누락() throws Exception {
        Map<String, Object> bad = body("C-3");
        bad.put("status", "없는상태");
        bad.remove("name");

        mvc.perform(post("/api/milestones").contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.fieldErrors", hasSize(2)));

        mvc.perform(patch("/api/milestones/1/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"예정\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 미존재_404() throws Exception {
        mvc.perform(get("/api/milestones/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    void 중복_code_409() throws Exception {
        create(body("C-DUP"));
        mvc.perform(post("/api/milestones").contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(body("C-DUP"))))
                .andExpect(status().isConflict());
    }
}
