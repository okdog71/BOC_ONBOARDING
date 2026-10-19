package com.grovesoft.boctracker.access;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.grovesoft.boctracker.support.ApiTestSupport;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

/** ③ 접근권한: 코드·순서, 신청/승인 일자 규칙, 소요일, 지연, 접근 전제 경고. */
class AccessItemRulesTest extends ApiTestSupport {

    private static final String API = "/api/access-items";

    private JsonNode item(String category, String target) throws Exception {
        return create(API, map("category", category, "target", target));
    }

    @Test
    void 코드는_AC_순번이고_구분별_No가_따로_매겨진다() throws Exception {
        JsonNode a = item("시스템", "dev 콘솔");
        JsonNode b = item("저장소", "Git");
        JsonNode c = item("시스템", "qa 콘솔");
        assertThat(List.of(a.get("code").asText(), b.get("code").asText(), c.get("code").asText()))
                .containsExactly("AC.1", "AC.2", "AC.3");
        assertThat(List.of(a.get("no").asInt(), b.get("no").asInt(), c.get("no").asInt())).containsExactly(1, 1, 2);

        send(put(API + "/order"), map("group", "시스템", "ids", List.of(c.get("id").asLong(), a.get("id").asLong())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data[0].code").value("AC.3"));
    }

    @Test
    void 상태_전이에_따라_신청일_승인일이_기록되고_소요일이_계산된다() throws Exception {
        long id = item("시스템", "dev 콘솔").get("id").asLong();
        String today = LocalDate.now().toString();

        setStatus(API, id, "신청").andExpect(jsonPath("$.data.requestedDate").value(today))
                .andExpect(jsonPath("$.data.approvedDate").doesNotExist());
        setStatus(API, id, "승인").andExpect(jsonPath("$.data.approvedDate").value(today))
                .andExpect(jsonPath("$.data.leadTimeDays").value(0))
                .andExpect(jsonPath("$.data.ready").value(true));
        setStatus(API, id, "거부").andExpect(jsonPath("$.data.approvedDate").doesNotExist())
                .andExpect(jsonPath("$.data.requestedDate").value(today));
        setStatus(API, id, "미신청").andExpect(jsonPath("$.data.requestedDate").doesNotExist());
    }

    @Test
    void 일자와_상태가_모순되면_400() throws Exception {
        send(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(API),
                map("category", "시스템", "target", "x", "requestedDate", "2026-11-02"))   // 미신청 + 신청일
                .andExpect(status().isBadRequest());
        send(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(API),
                map("category", "시스템", "target", "x", "status", "신청", "requestedDate", "2026-11-02",
                        "approvedDate", "2026-11-03"))   // 신청 + 승인일
                .andExpect(status().isBadRequest());
        send(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(API),
                map("category", "시스템", "target", "x", "status", "승인", "requestedDate", "2026-11-05",
                        "approvedDate", "2026-11-03"))   // 승인일 < 신청일
                .andExpect(status().isBadRequest());

        JsonNode ok = create(API, map("category", "시스템", "target", "x", "status", "승인",
                "requestedDate", "2026-11-02", "approvedDate", "2026-11-05"));
        assertThat(ok.get("leadTimeDays").asInt()).isEqualTo(3);
    }

    @Test
    void 지연은_필요일이_지났고_승인전일때만() throws Exception {
        long id = create(API, map("category", "시스템", "target", "x",
                "targetDate", LocalDate.now().minusDays(2).toString())).get("id").asLong();
        mvc.perform(get(API).param("overdue", "true")).andExpect(jsonPath("$.data.content", hasSize(1)));
        setStatus(API, id, "승인").andExpect(jsonPath("$.data.overdue").value(false));
        mvc.perform(get(API).param("overdue", "true")).andExpect(jsonPath("$.data.content", hasSize(0)));
    }

    @Test
    void 접근_전제가_미승인이면_다른_접근에_경고() throws Exception {
        long vpn = create(API, map("category", "네트워크", "target", "VPN", "usage", "접근 전제")).get("id").asLong();
        long dev = item("시스템", "dev 콘솔").get("id").asLong();

        setStatus(API, dev, "신청").andExpect(jsonPath("$.data.warnings[?(@ =~ /.*접근 전제.*/)]").exists());
        setStatus(API, vpn, "승인");
        setStatus(API, dev, "신청").andExpect(jsonPath("$.data.warnings[?(@ =~ /.*접근 전제.*/)]").doesNotExist());
        assertThat(fetch(API, vpn).get("prerequisite").asBoolean()).isTrue();
    }

    @Test
    void 거부는_사유_경고_코드는_변경불가() throws Exception {
        long id = item("시스템", "x").get("id").asLong();
        setStatus(API, id, "거부").andExpect(jsonPath("$.data.warnings", hasSize(1)));
        send(put(API + "/{id}", id), map("category", "시스템", "target", "x", "code", "AC.99"))
                .andExpect(status().isBadRequest());
    }
}
