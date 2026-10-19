package com.grovesoft.boctracker.techdebt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.grovesoft.boctracker.support.ApiTestSupport;
import com.grovesoft.boctracker.support.XlsxTestUtil;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class TechDebtApiTest extends ApiTestSupport {

    private static final String PATH = "/api/tech-debts";

    private Map<String, Object> body(String system, String priority, String name) {
        return map("system", system, "priority", priority, "name", name, "area", "연동/장애",
                "verifyCriteria", "회귀 테스트 통과");
    }

    private JsonNode make(String system, String priority, String name) throws Exception {
        return create(PATH, body(system, priority, name));
    }

    @Test
    void 코드는_시스템_접두로_자동_부여되고_시스템별로_이어진다() throws Exception {
        assertThat(make("KEC", "P1", "a").get("code").asText()).isEqualTo("KEC-01");
        assertThat(make("KEC", "P2", "b").get("code").asText()).isEqualTo("KEC-02");
        assertThat(make("BOC", "P1", "c").get("code").asText()).isEqualTo("BOC-01");
        assertThat(make("공통", "P3", "d").get("code").asText()).isEqualTo("CMN-01");
        JsonNode d = make("KEC", "P1", "e");
        assertThat(d.get("code").asText()).isEqualTo("KEC-03");
        // 기본 상태는 미착수, 같은 우선순위 안 No. 는 만든 순서
        assertThat(d.get("status").asText()).isEqualTo("미착수");
        assertThat(d.get("no").asInt()).isEqualTo(3);   // P1: a(1) c(2) e(3)
    }

    @Test
    void 명시한_코드가_중복이면_409_코드_변경은_400() throws Exception {
        Map<String, Object> b = body("KEC", "P1", "x");
        b.put("code", "KEC-07");
        JsonNode created = create(PATH, b);
        send(post(PATH), b).andExpect(status().isConflict());
        Map<String, Object> changed = body("KEC", "P1", "x");
        changed.put("code", "KEC-99");
        send(put(PATH + "/{id}", created.get("id").asLong()), changed).andExpect(status().isBadRequest());
    }

    @Test
    void 필수값과_허용값_검증() throws Exception {
        send(post(PATH), body("KEC", "P9", "x")).andExpect(status().isBadRequest());
        send(post(PATH), body("ABC", "P1", "x")).andExpect(status().isBadRequest());
        send(post(PATH), body("KEC", "P1", " ")).andExpect(status().isBadRequest());
        Map<String, Object> badCode = body("KEC", "P1", "x");
        badCode.put("code", "kec 1");
        send(post(PATH), badCode).andExpect(status().isBadRequest());
    }

    @Test
    void 완료는_담당이_있어야_하고_완료일은_서버가_기록하고_되돌리면_지운다() throws Exception {
        long id = make("KEC", "P1", "a").get("id").asLong();
        setStatus(PATH, id, "완료").andExpect(status().isBadRequest());
        assertThat(fetch(PATH, id).get("status").asText()).isEqualTo("미착수");

        Map<String, Object> withOwner = body("KEC", "P1", "a");
        withOwner.put("owner", "홍길동");
        send(put(PATH + "/{id}", id), withOwner).andExpect(status().isOk());
        JsonNode done = read(setStatus(PATH, id, "완료").andExpect(status().isOk())).get("data");
        assertThat(done.get("done").asBoolean()).isTrue();
        assertThat(done.get("completedDate").asText()).isEqualTo(LocalDate.now().toString());

        JsonNode back = read(setStatus(PATH, id, "진행중").andExpect(status().isOk())).get("data");
        assertThat(back.get("completedDate").isNull()).isTrue();
        assertThat(back.get("done").asBoolean()).isFalse();
    }

    @Test
    void PUT_으로_완료로_바꿀_때도_담당이_필요하다() throws Exception {
        long id = make("KEC", "P1", "a").get("id").asLong();
        Map<String, Object> b = body("KEC", "P1", "a");
        b.put("status", "완료");
        send(put(PATH + "/{id}", id), b).andExpect(status().isBadRequest());
        b.put("owner", "담당자");
        send(put(PATH + "/{id}", id), b).andExpect(status().isOk());
    }

    @Test
    void 우선순위를_바꾸면_새_그룹_맨_뒤로_가고_옛_그룹_번호는_다시_매겨지며_코드는_그대로다() throws Exception {
        JsonNode a = make("KEC", "P1", "a");
        JsonNode b = make("KEC", "P1", "b");
        JsonNode c = make("KEC", "P1", "c");
        JsonNode other = make("KEC", "P2", "o");
        Map<String, Object> moved = body("KEC", "P2", "a");
        send(put(PATH + "/{id}", a.get("id").asLong()), moved).andExpect(status().isOk());

        JsonNode movedNow = fetch(PATH, a.get("id").asLong());
        assertThat(movedNow.get("priority").asText()).isEqualTo("P2");
        assertThat(movedNow.get("no").asInt()).isEqualTo(other.get("no").asInt() + 1);
        assertThat(movedNow.get("code").asText()).isEqualTo(a.get("code").asText());
        assertThat(fetch(PATH, b.get("id").asLong()).get("no").asInt()).isEqualTo(1);
        assertThat(fetch(PATH, c.get("id").asLong()).get("no").asInt()).isEqualTo(2);
    }

    @Test
    void 순서_변경은_그룹의_전체_id가_있어야_한다() throws Exception {
        JsonNode a = make("KEC", "P1", "a");
        JsonNode b = make("KEC", "P1", "b");
        send(put(PATH + "/order"), map("group", "P1", "ids", List.of(b.get("id").asLong(), a.get("id").asLong())))
                .andExpect(status().isOk());
        assertThat(fetch(PATH, b.get("id").asLong()).get("no").asInt()).isEqualTo(1);
        send(put(PATH + "/order"), map("group", "P1", "ids", List.of(a.get("id").asLong()))).andExpect(status().isBadRequest());
    }

    @Test
    void 목표_시점이_지나고_끝나지_않았으면_지연이고_완료_보류는_지연이_아니다() throws Exception {
        Map<String, Object> b = body("KEC", "P1", "late");
        b.put("targetDate", LocalDate.now().minusDays(3).toString());
        b.put("owner", "담당");
        long id = create(PATH, b).get("id").asLong();
        assertThat(fetch(PATH, id).get("overdue").asBoolean()).isTrue();
        mvc.perform(get(PATH).param("overdue", "true")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("late")));
        setStatus(PATH, id, "보류");
        assertThat(fetch(PATH, id).get("overdue").asBoolean()).isFalse();
        setStatus(PATH, id, "완료");
        assertThat(fetch(PATH, id).get("overdue").asBoolean()).isFalse();
    }

    @Test
    void 경고_진행중인데_담당이_없고_P1_P2는_목표_시점이_없다() throws Exception {
        long id = make("KEC", "P1", "w").get("id").asLong();
        assertThat(fetch(PATH, id).get("warnings")).isEmpty();
        JsonNode w = read(setStatus(PATH, id, "진행중").andExpect(status().isOk())).get("data");
        assertThat(w.get("warnings")).hasSize(2);
    }

    @Test
    void 필터와_검색() throws Exception {
        make("KEC", "P1", "alpha");
        make("BOC", "P2", "beta");
        assertThat(read(mvc.perform(get(PATH).param("system", "BOC"))).at("/data/content")).hasSize(1);
        assertThat(read(mvc.perform(get(PATH).param("priority", "P1"))).at("/data/content")).hasSize(1);
        assertThat(read(mvc.perform(get(PATH).param("q", "ALP"))).at("/data/content")).hasSize(1);
        assertThat(read(mvc.perform(get(PATH).param("status", "완료"))).at("/data/content")).isEmpty();
    }

    @Test
    void 엑셀_내보내기는_필터를_따르고_값이_문자열로_들어간다() throws Exception {
        make("KEC", "P1", "=SUM(1,2)&<태그>");
        make("BOC", "P2", "다른 항목");
        byte[] all = mvc.perform(get(PATH + "/export")).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andReturn().getResponse().getContentAsByteArray();
        String sheet = XlsxTestUtil.sheetXml(all);
        assertThat(sheet).contains("우선순위").contains("다른 항목").contains("KEC-01")
                .contains("=SUM(1,2)&amp;&lt;태그&gt;")          // 이스케이프, 수식으로 해석되지 않는 inlineStr
                .doesNotContain("<f>");
        byte[] only = mvc.perform(get(PATH + "/export").param("system", "BOC")).andReturn().getResponse().getContentAsByteArray();
        assertThat(XlsxTestUtil.sheetXml(only)).contains("다른 항목").doesNotContain("SUM(1,2)");
    }

    private MockMultipartFile file(String name, String text) {
        return new MockMultipartFile("file", name, "text/plain", text.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void 첨부_업로드_다운로드_중복_차단_삭제() throws Exception {
        long id = make("KEC", "P1", "a").get("id").asLong();
        JsonNode after = read(mvc.perform(multipart(PATH + "/{id}/files", id).file(file("근거.txt", "내용 1")))
                .andExpect(status().isCreated())).get("data");
        assertThat(after.get("files")).hasSize(1);
        String url = after.at("/files/0/downloadUrl").asText();
        assertThat(url).isEqualTo(PATH + "/" + id + "/files/" + after.at("/files/0/id").asLong());
        assertThat(mvc.perform(get(url)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8)).isEqualTo("내용 1");

        mvc.perform(multipart(PATH + "/{id}/files", id).file(file("복사본.txt", "내용 1"))).andExpect(status().isConflict());
        mvc.perform(multipart(PATH + "/{id}/files", id).file(file("run.exe", "x"))).andExpect(status().isBadRequest());
        mvc.perform(multipart(PATH + "/{id}/files", id).file(file("빈.txt", ""))).andExpect(status().isBadRequest());
        mvc.perform(multipart(PATH + "/{id}/files", 999999).file(file("a.txt", "a"))).andExpect(status().isNotFound());

        // 다른 항목의 파일 id 로는 받을 수 없다
        long other = make("KEC", "P2", "b").get("id").asLong();
        mvc.perform(get(PATH + "/{id}/files/{f}", other, after.at("/files/0/id").asLong())).andExpect(status().isNotFound());

        mvc.perform(delete(url)).andExpect(status().isNoContent());
        assertThat(fetch(PATH, id).get("files")).isEmpty();
    }

    @Test
    void 항목을_지우면_첨부도_지워진다() throws Exception {
        long id = make("KEC", "P1", "a").get("id").asLong();
        mvc.perform(multipart(PATH + "/{id}/files", id).file(file("a.txt", "a"))).andExpect(status().isCreated());
        mvc.perform(delete(PATH + "/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get(PATH + "/{id}", id)).andExpect(status().isNotFound());
        // 같은 파일을 다른 항목에 다시 올릴 수 있다(첨부 행이 남아 있지 않다)
        long id2 = make("KEC", "P1", "b").get("id").asLong();
        mvc.perform(multipart(PATH + "/{id}/files", id2).file(file("a.txt", "a"))).andExpect(status().isCreated());
    }

    @Test
    void 그룹명_일괄_변경은_허용된_우선순위로만_가능하다() throws Exception {
        make("KEC", "P3", "a");
        send(patch(PATH + "/groups"), Map.of("from", "P3", "to", "긴급")).andExpect(status().isBadRequest());
        send(patch(PATH + "/groups"), Map.of("from", "P3", "to", "P4")).andExpect(status().isOk());
        assertThat(read(mvc.perform(get(PATH).param("priority", "P4"))).at("/data/content")).hasSize(1);
    }

    @Test
    void 상태_변경_허용값_검증과_404() throws Exception {
        long id = make("KEC", "P1", "a").get("id").asLong();
        send(patch(PATH + "/{id}/status", id), Map.of("status", "없는상태")).andExpect(status().isBadRequest());
        send(patch(PATH + "/{id}/status", 999999), Map.of("status", "확인중")).andExpect(status().isNotFound());
    }
}
