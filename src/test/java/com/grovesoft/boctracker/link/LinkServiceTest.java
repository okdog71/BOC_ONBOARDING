package com.grovesoft.boctracker.link;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.grovesoft.boctracker.support.ApiTestSupport;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** 메뉴·문서 링크: 마일스톤 산출물(②③⑤) → 실제 진행 현황, 링크 점검(audit). */
class LinkServiceTest extends ApiTestSupport {

    private long supply(String name) throws Exception {
        return create("/api/supply-docs", map("category", "A. 기획", "name", name)).get("id").asLong();
    }

    @Test
    void 마일스톤_산출물_기호가_가리키는_메뉴의_진행률을_보여준다() throws Exception {
        long ms = create("/api/milestones", map("phase", "P0. 수급", "name", "수급 확인", "output", "수급 산출물 확인표(②)"))
                .get("id").asLong();
        long a = supply("a");
        supply("b");
        setStatus("/api/supply-docs", a, "수령");

        mvc.perform(get("/api/milestones/{id}", ms))
                .andExpect(jsonPath("$.data.links", hasSize(1)))
                .andExpect(jsonPath("$.data.links[0].sheet").value("②"))
                .andExpect(jsonPath("$.data.links[0].route").value("/supply-docs"))
                .andExpect(jsonPath("$.data.links[0].done").value(1))
                .andExpect(jsonPath("$.data.links[0].total").value(2));
    }

    @Test
    void 인수인계_마일스톤은_연결된_세션의_완료율_연결이_없으면_일정_확정률() throws Exception {
        JsonNode ho = create("/api/milestones", map("phase", "HO. 인수인계", "name", "Week1", "output", "KT Plan(⑤)"));
        JsonNode plan = create("/api/milestones", map("phase", "P0. 수급", "name", "일정 협의", "output", "KT Plan(⑤)"));
        long s1 = create("/api/handover-sessions", map("week", "W1", "category", "공통", "topic", "a",
                "milestoneCode", ho.get("code").asText(), "plannedDate", "2099-01-01")).get("id").asLong();
        create("/api/handover-sessions", map("week", "W1", "category", "공통", "topic", "b",
                "milestoneCode", ho.get("code").asText()));
        setStatus("/api/handover-sessions", s1, "완료");

        mvc.perform(get("/api/milestones/{id}", ho.get("id").asLong()))
                .andExpect(jsonPath("$.data.links[0].metric").value("완료"))
                .andExpect(jsonPath("$.data.links[0].done").value(1))
                .andExpect(jsonPath("$.data.links[0].total").value(2));
        mvc.perform(get("/api/milestones/{id}", plan.get("id").asLong()))
                .andExpect(jsonPath("$.data.links[0].metric").value("예정일 확정"))
                .andExpect(jsonPath("$.data.links[0].done").value(1))
                .andExpect(jsonPath("$.data.links[0].total").value(2));
    }

    @Test
    void 링크_점검은_끊어진_연결을_알려주고_연결하면_사라진다() throws Exception {
        create("/api/handover-sessions", map("week", "W1", "category", "공통", "topic", "환경",
                "precondition", "수급: 배포구조문서 / 접근: ArgoCD(③)"));
        create("/api/milestones", map("phase", "P0. 수급", "name", "x", "precondition", "P0.1,P9.9"));

        JsonNode report = read(mvc.perform(get("/api/summary/links")).andExpect(status().isOk())).get("data");
        List<String> messages = report.get("issues").findValuesAsText("message");
        org.assertj.core.api.Assertions.assertThat(messages).anyMatch(m -> m.contains("수급 산출물(②)이 없습니다"));
        org.assertj.core.api.Assertions.assertThat(messages).anyMatch(m -> m.contains("접근권한(③)이 없습니다"));
        org.assertj.core.api.Assertions.assertThat(messages).anyMatch(m -> m.contains("연결된 인수인계 마일스톤이 없습니다"));
        org.assertj.core.api.Assertions.assertThat(messages).anyMatch(m -> m.contains("P9.9"));
        org.assertj.core.api.Assertions.assertThat(report.get("checks")).hasSize(5);
    }

    @Test
    void 정상_연결이면_점검_결과가_비어_있다() throws Exception {
        create("/api/milestones", map("phase", "HO. 인수인계", "name", "W1", "output", "KT Plan(⑤)"));
        supply("배포 구조");
        create("/api/handover-sessions", map("week", "W1", "category", "공통", "topic", "환경",
                "precondition", "수급: 배포구조문서", "milestoneCode", "HO.1", "supplyDocCodes", List.of("A.1")));

        mvc.perform(get("/api/summary/links")).andExpect(jsonPath("$.data.issues", hasSize(0)));
        Map<String, Object> unused = map();
        org.assertj.core.api.Assertions.assertThat(unused).isEmpty();
    }
}
