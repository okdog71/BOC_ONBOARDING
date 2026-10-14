package com.grovesoft.boctracker.menu;

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
class MenuInventoryControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    private Map<String, Object> body() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("gnb", "운영");
        m.put("menu", "쿠폰 관리");
        m.put("submenu", "쿠폰 등록");
        m.put("path", "/redeem/coupon");
        m.put("domain", "redeem");
        m.put("backendService", "redeem-api");
        m.put("dataStore", "MySQL");
        m.put("externalLink", "결제 게이트웨이");
        m.put("permission", "운영자");
        m.put("generation", "A");
        m.put("priority", "상");
        m.put("knownIssue", "KEC-01");
        m.put("devCheck", "확인");
        m.put("owner", "BOC 리드");
        return m;
    }

    private long create(Map<String, Object> body) throws Exception {
        String res = mvc.perform(post("/api/menu-inventories").contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return om.readTree(res).at("/data/id").asLong();
    }

    @Test
    void 생성_조회_목록_필터_happy_path() throws Exception {
        long id = create(body());
        Map<String, Object> other = body();
        other.put("priority", "하");
        other.put("domain", "order");
        other.put("devCheck", "mock");
        other.put("analysisStatus", "완료");
        create(other);

        mvc.perform(get("/api/menu-inventories/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.gnb").value("운영"))
                .andExpect(jsonPath("$.data.menu").value("쿠폰 관리"))
                .andExpect(jsonPath("$.data.submenu").value("쿠폰 등록"))
                .andExpect(jsonPath("$.data.path").value("/redeem/coupon"))
                .andExpect(jsonPath("$.data.domain").value("redeem"))
                .andExpect(jsonPath("$.data.backendService").value("redeem-api"))
                .andExpect(jsonPath("$.data.dataStore").value("MySQL"))
                .andExpect(jsonPath("$.data.externalLink").value("결제 게이트웨이"))
                .andExpect(jsonPath("$.data.permission").value("운영자"))
                .andExpect(jsonPath("$.data.generation").value("A"))
                .andExpect(jsonPath("$.data.priority").value("상"))
                .andExpect(jsonPath("$.data.knownIssue").value("KEC-01"))
                .andExpect(jsonPath("$.data.devCheck").value("확인"))
                .andExpect(jsonPath("$.data.owner").value("BOC 리드"))
                .andExpect(jsonPath("$.data.analysisStatus").value("미착수"));

        mvc.perform(get("/api/menu-inventories").param("status", "미착수"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value(id));
        mvc.perform(get("/api/menu-inventories").param("priority", "상"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value(id));
        mvc.perform(get("/api/menu-inventories").param("domain", "redeem"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value(id));
        mvc.perform(get("/api/menu-inventories").param("devCheck", "확인"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value(id));
        mvc.perform(get("/api/menu-inventories"))
                .andExpect(jsonPath("$.data.totalElements").value(2));
        mvc.perform(get("/api/menu-inventories").param("status", "없는값"))
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    void 수정_상태변경_삭제() throws Exception {
        long id = create(body());
        Map<String, Object> updated = body();
        updated.put("menu", "수정된 값");
        mvc.perform(put("/api/menu-inventories/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.menu").value("수정된 값"));

        mvc.perform(patch("/api/menu-inventories/{id}/status", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"완료\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.analysisStatus").value("완료"));

        mvc.perform(delete("/api/menu-inventories/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/menu-inventories/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void 검증실패_400() throws Exception {
        Map<String, Object> bad = body();
        bad.remove("menu");
        bad.put("generation", "없는값");
        mvc.perform(post("/api/menu-inventories").contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.fieldErrors", hasSize(2)));

        mvc.perform(patch("/api/menu-inventories/1/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"없는값\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 미존재_404() throws Exception {
        mvc.perform(get("/api/menu-inventories/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }
}
