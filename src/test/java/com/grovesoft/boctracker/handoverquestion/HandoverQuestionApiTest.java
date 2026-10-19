package com.grovesoft.boctracker.handoverquestion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.grovesoft.boctracker.support.ApiTestSupport;
import com.grovesoft.boctracker.support.XlsxTestUtil;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class HandoverQuestionApiTest extends ApiTestSupport {

    private static final String PATH = "/api/handover-questions";

    private Map<String, Object> body(String priority, String question) {
        return map("priority", priority, "question", question, "kind", "추정 I/F", "askTo", "KEC 개발 리드",
                "hypothesis", "스케줄러일 것이다");
    }

    private JsonNode make(String priority, String question) throws Exception {
        return create(PATH, body(priority, question));
    }

    @Test
    void 코드는_Q_번호로_자동_부여된다() throws Exception {
        assertThat(make("높음", "a").get("code").asText()).isEqualTo("Q-01");
        assertThat(make("낮음", "b").get("code").asText()).isEqualTo("Q-02");
        JsonNode c = make("높음", "c");
        assertThat(c.get("code").asText()).isEqualTo("Q-03");
        assertThat(c.get("no").asInt()).isEqualTo(2);
        assertThat(c.get("status").asText()).isEqualTo("미확인");
        assertThat(c.get("overdue").asBoolean()).isFalse();
        assertThat(c.get("hypothesis").asText()).isEqualTo("스케줄러일 것이다");
    }

    @Test
    void 필수값과_허용값_검증() throws Exception {
        send(post(PATH), body("긴급", "q")).andExpect(status().isBadRequest());
        send(post(PATH), body("높음", "")).andExpect(status().isBadRequest());
        Map<String, Object> badStatus = body("높음", "q");
        badStatus.put("status", "끝");
        send(post(PATH), badStatus).andExpect(status().isBadRequest());
    }

    @Test
    void 확인완료는_답변과_확인자가_있어야_하고_확인일은_서버가_기록한다() throws Exception {
        long id = make("높음", "q").get("id").asLong();
        setStatus(PATH, id, "확인완료").andExpect(status().isBadRequest());

        Map<String, Object> onlyAnswer = body("높음", "q");
        onlyAnswer.put("answer", "배치는 WIN 상태만 만든다");
        send(put(PATH + "/{id}", id), onlyAnswer).andExpect(status().isOk());
        setStatus(PATH, id, "확인완료").andExpect(status().isBadRequest());   // 확인자 없음

        onlyAnswer.put("confirmedBy", "홍길동");
        send(put(PATH + "/{id}", id), onlyAnswer).andExpect(status().isOk());
        JsonNode done = read(setStatus(PATH, id, "확인완료").andExpect(status().isOk())).get("data");
        assertThat(done.get("done").asBoolean()).isTrue();
        assertThat(done.get("confirmedDate").asText()).isEqualTo(LocalDate.now().toString());

        // 되돌리면 확인일은 지워지고 답변은 남는다
        JsonNode back = read(setStatus(PATH, id, "진행중").andExpect(status().isOk())).get("data");
        assertThat(back.get("confirmedDate").isNull()).isTrue();
        assertThat(back.get("answer").asText()).isEqualTo("배치는 WIN 상태만 만든다");
    }

    @Test
    void 확인완료로_저장할_때_확인일을_직접_줄_수_있고_수정해도_유지된다() throws Exception {
        long id = make("중", "q").get("id").asLong();
        Map<String, Object> b = body("중", "q");
        b.put("status", "확인완료");
        b.put("answer", "확인함");
        b.put("confirmedBy", "담당");
        b.put("confirmedDate", "2026-10-01");
        send(put(PATH + "/{id}", id), b).andExpect(status().isOk());
        assertThat(fetch(PATH, id).get("confirmedDate").asText()).isEqualTo("2026-10-01");
        b.remove("confirmedDate");            // 날짜 없이 다시 저장해도 기존 확인일 유지
        b.put("note", "메모 추가");
        send(put(PATH + "/{id}", id), b).andExpect(status().isOk());
        assertThat(fetch(PATH, id).get("confirmedDate").asText()).isEqualTo("2026-10-01");
    }

    @Test
    void 경고_답변이_있는데_미확인_진행중인데_확인_대상이_없음() throws Exception {
        Map<String, Object> b = body("높음", "q");
        b.put("answer", "답");
        JsonNode created = create(PATH, b);
        assertThat(created.get("warnings")).hasSize(1);
        Map<String, Object> c = body("높음", "r");
        c.put("askTo", "");
        c.put("status", "진행중");
        assertThat(create(PATH, c).get("warnings")).hasSize(1);
    }

    @Test
    void 우선순위_이동_코드_불변_삭제_후_번호_재정렬() throws Exception {
        JsonNode a = make("높음", "a");
        JsonNode b = make("높음", "b");
        make("높음", "c");
        send(put(PATH + "/{id}", a.get("id").asLong()), body("중", "a")).andExpect(status().isOk());
        assertThat(fetch(PATH, a.get("id").asLong()).get("code").asText()).isEqualTo("Q-01");
        assertThat(fetch(PATH, b.get("id").asLong()).get("no").asInt()).isEqualTo(1);
        mvc.perform(delete(PATH + "/{id}", b.get("id").asLong())).andExpect(status().isNoContent());
        // 중간 번호(Q-02)를 지워도 최대 번호 기준이라 재사용하지 않는다
        assertThat(make("높음", "n").get("code").asText()).isEqualTo("Q-04");
    }

    @Test
    void 엑셀_내보내기_우선순위_순서와_가설_머리글() throws Exception {
        make("낮음", "낮은 질문");
        make("높음", "높은 질문");
        byte[] xlsx = mvc.perform(get(PATH + "/export")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();
        String sheet = XlsxTestUtil.sheetXml(xlsx);
        assertThat(sheet).contains("가설 (확인 전 추정·사실 아님)");
        assertThat(sheet.indexOf("높은 질문")).isLessThan(sheet.indexOf("낮은 질문"));
    }

    @Test
    void 첨부_업로드_다운로드_삭제() throws Exception {
        long id = make("높음", "q").get("id").asLong();
        MockMultipartFile f = new MockMultipartFile("file", "회신.txt", "text/plain", "회신 본문".getBytes(StandardCharsets.UTF_8));
        JsonNode after = read(mvc.perform(multipart(PATH + "/{id}/files", id).file(f)).andExpect(status().isCreated())).get("data");
        String url = after.at("/files/0/downloadUrl").asText();
        assertThat(url).startsWith(PATH + "/" + id + "/files/");
        assertThat(mvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8)).isEqualTo("회신 본문");
        mvc.perform(delete(url)).andExpect(status().isNoContent());
        assertThat(fetch(PATH, id).get("files")).isEmpty();
    }
}
