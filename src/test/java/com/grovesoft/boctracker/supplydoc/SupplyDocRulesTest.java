package com.grovesoft.boctracker.supplydoc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.grovesoft.boctracker.support.ApiTestSupport;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** ② 수급 산출물: 코드·순서, 상태 전이 일자, 지연, 경고, 삭제 보호. */
class SupplyDocRulesTest extends ApiTestSupport {

    private static final String API = "/api/supply-docs";

    private JsonNode doc(String category, String name) throws Exception {
        return create(API, map("category", category, "name", name));
    }

    @Test
    void 코드는_구분_알파벳으로_자동_부여되고_번호는_재사용하지_않는다() throws Exception {
        JsonNode a1 = doc("A. 기획·화면", "a1");
        JsonNode a2 = doc("A. 기획·화면", "a2");
        JsonNode b1 = doc("B. 설계·기술", "b1");
        JsonNode x1 = doc("기타", "x1");
        assertThat(List.of(a1.get("code").asText(), a2.get("code").asText(), b1.get("code").asText(),
                x1.get("code").asText())).containsExactly("A.1", "A.2", "B.1", "S.1");

        mvc.perform(delete(API + "/{id}", a1.get("id").asLong())).andExpect(status().isNoContent());
        assertThat(doc("A. 기획·화면", "a3").get("code").asText()).isEqualTo("A.3");
    }

    @Test
    void 구분_내_No는_맨뒤_추가_삭제시_촘촘히_순서변경은_코드_유지() throws Exception {
        long a = doc("A. 기획", "a").get("id").asLong();
        long b = doc("A. 기획", "b").get("id").asLong();
        long c = doc("A. 기획", "c").get("id").asLong();
        assertThat(fetch(API, c).get("no").asInt()).isEqualTo(3);

        send(put(API + "/order"), map("group", "A. 기획", "ids", List.of(c, a, b))).andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").value("A.3"))
                .andExpect(jsonPath("$.data[0].no").value(1));

        mvc.perform(delete(API + "/{id}", c)).andExpect(status().isNoContent());
        assertThat(fetch(API, a).get("no").asInt()).isEqualTo(1);
        assertThat(fetch(API, b).get("no").asInt()).isEqualTo(2);

        send(put(API + "/order"), map("group", "A. 기획", "ids", List.of(a))).andExpect(status().isBadRequest());
        send(put(API + "/order"), map("group", "없는구분", "ids", List.of(a))).andExpect(status().isNotFound());
    }

    @Test
    void 코드는_변경할_수_없다_구분을_옮기면_새_구분_맨뒤_코드_유지() throws Exception {
        long a = doc("A. 기획", "a").get("id").asLong();
        doc("A. 기획", "a2");
        long b = doc("B. 설계", "b").get("id").asLong();

        send(put(API + "/{id}", a), map("category", "A. 기획", "name", "a", "code", "A.9"))
                .andExpect(status().isBadRequest());
        send(put(API + "/{id}", a), map("category", "B. 설계", "name", "a"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("A.1"))
                .andExpect(jsonPath("$.data.no").value(2));
        assertThat(fetch(API, b).get("no").asInt()).isEqualTo(1);
    }

    @Test
    void 상태_전이에_따라_요청일_수령일이_기록된다() throws Exception {
        long id = doc("A. 기획", "a").get("id").asLong();
        String today = LocalDate.now().toString();

        setStatus(API, id, "요청").andExpect(jsonPath("$.data.requestedDate").value(today))
                .andExpect(jsonPath("$.data.receivedDate").doesNotExist());
        setStatus(API, id, "수령").andExpect(jsonPath("$.data.receivedDate").value(today))
                .andExpect(jsonPath("$.data.ready").value(true));
        setStatus(API, id, "요청").andExpect(jsonPath("$.data.receivedDate").doesNotExist());
        setStatus(API, id, "미요청").andExpect(jsonPath("$.data.requestedDate").doesNotExist());
        setStatus(API, id, "해당없음").andExpect(jsonPath("$.data.ready").value(true));
    }

    @Test
    void 지연은_필요일이_지났고_수령전일때만() throws Exception {
        long id = create(API, map("category", "A. 기획", "name", "a",
                "targetDate", LocalDate.now().minusDays(3).toString())).get("id").asLong();
        mvc.perform(get(API + "/{id}", id)).andExpect(jsonPath("$.data.overdue").value(true));
        mvc.perform(get(API).param("overdue", "true")).andExpect(jsonPath("$.data.content", hasSize(1)));

        setStatus(API, id, "수령").andExpect(jsonPath("$.data.overdue").value(false));
        mvc.perform(get(API).param("overdue", "true")).andExpect(jsonPath("$.data.content", hasSize(0)));
    }

    @Test
    void 수령했는데_정보가_부족하면_경고() throws Exception {
        long id = doc("A. 기획", "a").get("id").asLong();
        setStatus(API, id, "수령")
                .andExpect(jsonPath("$.data.warnings", hasSize(4)))
                .andExpect(jsonPath("$.data.warnings[0]", containsString("존재 여부")));

        send(put(API + "/{id}", id), map("category", "A. 기획", "name", "a", "supplyStatus", "수령",
                "existence", "있음", "location", "Confluence", "freshness", "현행", "owner", "기획"))
                .andExpect(jsonPath("$.data.warnings", hasSize(0)));
    }

    @Test
    void 구분명_일괄_변경과_검색_필터() throws Exception {
        doc("A. 기획", "스토리보드");
        doc("A. 기획", "IA");
        send(patch(API + "/groups"), map("from", "A. 기획", "to", "A. 기획·화면")).andExpect(status().isOk());
        mvc.perform(get(API).param("category", "A. 기획·화면")).andExpect(jsonPath("$.data.content", hasSize(2)));
        mvc.perform(get(API).param("q", "스토리")).andExpect(jsonPath("$.data.content", hasSize(1)));
        send(patch(API + "/groups"), Map.of("from", "없음", "to", "x")).andExpect(status().isNotFound());
    }
}
