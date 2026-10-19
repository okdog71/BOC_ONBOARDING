package com.grovesoft.boctracker.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grovesoft.boctracker.common.meta.CodeValues;
import jakarta.persistence.EntityManagerFactory;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
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
class SummaryControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;
    @Autowired
    EntityManagerFactory emf;

    private JsonNode summary() throws Exception {
        String res = mvc.perform(get("/api/summary")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode root = om.readTree(res);
        assertThat(root.at("/success").asBoolean()).isTrue();
        return root.at("/data");
    }

    private void postJson(String url, Map<String, Object> body) throws Exception {
        mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(body)))
                .andExpect(status().isCreated());
    }

    private static List<String> keys(JsonNode obj) {
        List<String> k = new ArrayList<>();
        Iterator<String> it = obj.fieldNames();
        it.forEachRemaining(k::add);
        return k;
    }

    @Test
    void 구조_모든_허용값이_코드값_순서대로_0건이라도_포함된다() throws Exception {
        JsonNode s = summary();

        assertThat(keys(s)).containsExactly("milestone", "handover", "catchup", "supply", "access", "menu",
                "issue", "raci", "review", "analysisCard", "megaProcess", "interfaceLink", "docReliability", "techDebt",
                "handoverQuestion");
        assertThat(keys(s.at("/techDebt/byStatus"))).containsExactlyElementsOf(CodeValues.TECH_DEBT_STATUS);
        assertThat(keys(s.at("/techDebt/byPriority"))).containsExactlyElementsOf(CodeValues.TECH_DEBT_PRIORITY);
        assertThat(keys(s.at("/handoverQuestion/byStatus"))).containsExactlyElementsOf(CodeValues.QUESTION_STATUS);
        assertThat(keys(s.at("/handoverQuestion/byPriority"))).containsExactlyElementsOf(CodeValues.QUESTION_PRIORITY);
        assertThat(keys(s.at("/milestone/byStatus"))).containsExactlyElementsOf(CodeValues.MILESTONE_STATUS);
        assertThat(keys(s.at("/handover/byStatus"))).containsExactlyElementsOf(CodeValues.HANDOVER_STATUS);
        assertThat(keys(s.at("/supply/byStatus"))).containsExactlyElementsOf(CodeValues.SUPPLY_STATUS);
        assertThat(keys(s.at("/access/byStatus"))).containsExactlyElementsOf(CodeValues.ACCESS_STATUS);
        assertThat(keys(s.at("/issue/byStatus"))).containsExactlyElementsOf(CodeValues.ISSUE_STATUS);
        assertThat(keys(s.at("/menu/byStatus"))).containsExactlyElementsOf(CodeValues.MILESTONE_STATUS);
        assertThat(keys(s.at("/menu/byPriority"))).startsWith("상", "중", "하");

        // byPriority 는 menu 에만, status 없는 트래커는 byStatus 도 없다
        assertThat(s.at("/milestone").has("byPriority")).isFalse();
        assertThat(s.at("/megaProcess").has("byStatus")).isFalse();
        assertThat(s.at("/megaProcess/total").isNumber()).isTrue();

        // 각 섹션: byStatus 합 == total
        for (String d : List.of("milestone", "handover", "catchup", "supply", "access", "menu", "issue", "raci",
                "review", "analysisCard")) {
            long sum = 0;
            for (JsonNode n : s.at("/" + d + "/byStatus")) {
                sum += n.asLong();
            }
            assertThat(sum).as(d).isEqualTo(s.at("/" + d + "/total").asLong());
        }
    }

    @Test
    void 데이터를_넣으면_해당_버킷만_증가한다() throws Exception {
        JsonNode before = summary();

        postJson("/api/milestones", Map.of("phase", "P0", "code", "SUM-1", "name", "집계 테스트", "status", "완료"));
        postJson("/api/issue-queues", Map.of("item", "SUM-I1", "type", "이슈", "status", "확인중"));
        postJson("/api/supply-docs", Map.of("category", "A", "name", "문서"));              // 기본 미요청
        postJson("/api/menu-inventories", Map.of("menu", "m1", "priority", "상"));          // 기본 미착수
        postJson("/api/menu-inventories", Map.of("menu", "m2", "priority", "상", "analysisStatus", "진행중"));
        postJson("/api/menu-inventories", Map.of("menu", "m3"));                            // priority 미지정
        postJson("/api/mega-processes", Map.of("l0", "리딤/주문", "l1", "쿠폰"));

        JsonNode after = summary();
        assertThat(delta(before, after, "/milestone/total")).isEqualTo(1);
        assertThat(delta(before, after, "/milestone/byStatus/완료")).isEqualTo(1);
        assertThat(delta(before, after, "/milestone/byStatus/미착수")).isZero();
        assertThat(delta(before, after, "/issue/byStatus/확인중")).isEqualTo(1);
        assertThat(delta(before, after, "/supply/byStatus/미요청")).isEqualTo(1);
        assertThat(delta(before, after, "/menu/total")).isEqualTo(3);
        assertThat(delta(before, after, "/menu/byStatus/미착수")).isEqualTo(2);
        assertThat(delta(before, after, "/menu/byStatus/진행중")).isEqualTo(1);
        assertThat(delta(before, after, "/menu/byPriority/상")).isEqualTo(2);
        assertThat(delta(before, after, "/menu/byPriority/중")).isZero();
        assertThat(after.at("/menu/byPriority/미지정").asLong()).isEqualTo(1);
        assertThat(delta(before, after, "/megaProcess/total")).isEqualTo(1);
        assertThat(delta(before, after, "/catchup/total")).isZero();
    }

    private long delta(JsonNode before, JsonNode after, String pointer) {
        return after.at(pointer).asLong() - before.at(pointer).asLong();
    }

    @Test
    void 쿼리는_도메인당_1쿼리_menu만_2쿼리_총_18번을_넘지_않는다() throws Exception {
        Statistics stats = emf.unwrap(SessionFactory.class).getStatistics();
        boolean wasEnabled = stats.isStatisticsEnabled();
        stats.setStatisticsEnabled(true);
        try {
            stats.clear();
            summary();
            assertThat(stats.getPrepareStatementCount()).isLessThanOrEqualTo(18);
        } finally {
            stats.setStatisticsEnabled(wasEnabled);
        }
    }
}
