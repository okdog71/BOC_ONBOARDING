package com.grovesoft.boctracker.analysiscard;

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
class AnalysisCardControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    private Map<String, Object> body() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("menuName", "쿠폰 등록");
        m.put("path", "/redeem/coupon");
        m.put("domain", "redeem");
        m.put("backendService", "redeem-api");
        m.put("generation", "A");
        m.put("summary", "쿠폰 코드를 등록한다");
        m.put("scenario", "운영자가 코드를 입력하고 검증 후 저장");
        m.put("input", "쿠폰 코드");
        m.put("output", "등록 결과");
        m.put("backendMapping", "POST /coupons");
        m.put("dataStore", "MySQL");
        m.put("externalLink", "결제 게이트웨이");
        m.put("permission", "운영자");
        m.put("relatedProcess", "쿠폰 리딤");
        m.put("knownIssue", "KEC-01");
        m.put("devCheck", "확인");
        m.put("risk", "중복 등록");
        m.put("owner", "BOC 리드");
        m.put("analyzedDate", "2026-11-05");
        return m;
    }

    private long create(Map<String, Object> body) throws Exception {
        String res = mvc.perform(post("/api/analysis-cards").contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return om.readTree(res).at("/data/id").asLong();
    }

    @Test
    void 생성_조회_목록_필터_happy_path() throws Exception {
        long id = create(body());
        Map<String, Object> other = body();
        other.put("domain", "order");
        other.put("status", "완료");
        create(other);

        mvc.perform(get("/api/analysis-cards/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.menuName").value("쿠폰 등록"))
                .andExpect(jsonPath("$.data.path").value("/redeem/coupon"))
                .andExpect(jsonPath("$.data.domain").value("redeem"))
                .andExpect(jsonPath("$.data.backendService").value("redeem-api"))
                .andExpect(jsonPath("$.data.generation").value("A"))
                .andExpect(jsonPath("$.data.summary").value("쿠폰 코드를 등록한다"))
                .andExpect(jsonPath("$.data.scenario").value("운영자가 코드를 입력하고 검증 후 저장"))
                .andExpect(jsonPath("$.data.input").value("쿠폰 코드"))
                .andExpect(jsonPath("$.data.output").value("등록 결과"))
                .andExpect(jsonPath("$.data.backendMapping").value("POST /coupons"))
                .andExpect(jsonPath("$.data.dataStore").value("MySQL"))
                .andExpect(jsonPath("$.data.externalLink").value("결제 게이트웨이"))
                .andExpect(jsonPath("$.data.permission").value("운영자"))
                .andExpect(jsonPath("$.data.relatedProcess").value("쿠폰 리딤"))
                .andExpect(jsonPath("$.data.knownIssue").value("KEC-01"))
                .andExpect(jsonPath("$.data.devCheck").value("확인"))
                .andExpect(jsonPath("$.data.risk").value("중복 등록"))
                .andExpect(jsonPath("$.data.owner").value("BOC 리드"))
                .andExpect(jsonPath("$.data.analyzedDate").value("2026-11-05"))
                .andExpect(jsonPath("$.data.status").value("미착수"));

        mvc.perform(get("/api/analysis-cards").param("status", "미착수"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value(id));
        mvc.perform(get("/api/analysis-cards").param("domain", "redeem"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value(id));
        mvc.perform(get("/api/analysis-cards"))
                .andExpect(jsonPath("$.data.totalElements").value(2));
        mvc.perform(get("/api/analysis-cards").param("status", "없는값"))
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    void 수정_상태변경_삭제() throws Exception {
        long id = create(body());
        Map<String, Object> updated = body();
        updated.put("menuName", "수정된 값");
        mvc.perform(put("/api/analysis-cards/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.menuName").value("수정된 값"));

        mvc.perform(patch("/api/analysis-cards/{id}/status", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"완료\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("완료"));

        mvc.perform(delete("/api/analysis-cards/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/analysis-cards/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void 검증실패_400() throws Exception {
        Map<String, Object> bad = body();
        bad.remove("menuName");
        bad.put("generation", "없는값");
        mvc.perform(post("/api/analysis-cards").contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.fieldErrors", hasSize(2)));

        mvc.perform(patch("/api/analysis-cards/1/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"없는값\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 미존재_404() throws Exception {
        mvc.perform(get("/api/analysis-cards/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }
}
