package com.grovesoft.boctracker.megaprocess;

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
class MegaProcessControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    private Map<String, Object> body() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("l0", "리딤/주문");
        m.put("l1", "쿠폰 리딤");
        m.put("l2", "코드 검증");
        m.put("relatedMenu", "리딤 > 쿠폰 등록");
        return m;
    }

    private long create(Map<String, Object> body) throws Exception {
        String res = mvc.perform(post("/api/mega-processes").contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return om.readTree(res).at("/data/id").asLong();
    }

    @Test
    void 생성_조회_목록_필터_happy_path() throws Exception {
        long id = create(body());
        Map<String, Object> other = body();
        other.put("l0", "이벤트 운영");
        create(other);

        mvc.perform(get("/api/mega-processes/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.l0").value("리딤/주문"))
                .andExpect(jsonPath("$.data.l1").value("쿠폰 리딤"))
                .andExpect(jsonPath("$.data.l2").value("코드 검증"))
                .andExpect(jsonPath("$.data.relatedMenu").value("리딤 > 쿠폰 등록"));

        mvc.perform(get("/api/mega-processes").param("l0", "리딤/주문"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value(id));
        mvc.perform(get("/api/mega-processes"))
                .andExpect(jsonPath("$.data.totalElements").value(2));
        mvc.perform(get("/api/mega-processes").param("l0", "없는값"))
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    void 수정_삭제() throws Exception {
        long id = create(body());
        Map<String, Object> updated = body();
        updated.put("l0", "수정된 값");
        mvc.perform(put("/api/mega-processes/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.l0").value("수정된 값"));

        mvc.perform(delete("/api/mega-processes/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/mega-processes/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void 검증실패_400() throws Exception {
        Map<String, Object> bad = body();
        bad.remove("l0");
        bad.remove("l1");
        mvc.perform(post("/api/mega-processes").contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.fieldErrors", hasSize(2)));
    }

    @Test
    void 미존재_404() throws Exception {
        mvc.perform(get("/api/mega-processes/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }
}
