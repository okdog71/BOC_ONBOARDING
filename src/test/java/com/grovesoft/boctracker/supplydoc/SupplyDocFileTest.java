package com.grovesoft.boctracker.supplydoc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.grovesoft.boctracker.support.ApiTestSupport;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;

/** ② 수급 산출물 첨부 파일: 업로드·다운로드·삭제와 안전 규칙 */
class SupplyDocFileTest extends ApiTestSupport {

    private static final String BASE = "/api/supply-docs";

    private long newDoc() throws Exception {
        return create(BASE, map("category", "A. 테스트", "name", "첨부 시험", "supplyStatus", "수령",
                "existence", "있음", "freshness", "현행", "owner", "담당")).get("id").asLong();
    }

    private ResultActions upload(long id, String name, byte[] body) throws Exception {
        return mvc.perform(multipart(BASE + "/{id}/files", id)
                .file(new MockMultipartFile("file", name, "application/pdf", body)));
    }

    @Test
    void 업로드하면_산출물_응답에_파일이_담기고_다운로드된다() throws Exception {
        long id = newDoc();
        byte[] body = "배포 구조 문서 본문".getBytes(StandardCharsets.UTF_8);
        upload(id, "배포구조.pdf", body)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.files", hasSize(1)))
                .andExpect(jsonPath("$.data.files[0].name").value("배포구조.pdf"))
                .andExpect(jsonPath("$.data.files[0].size").value(body.length));

        JsonNode file = fetch(BASE, id).get("files").get(0);
        mvc.perform(get(file.get("downloadUrl").asText()))
                .andExpect(status().isOk())
                .andExpect(content().bytes(body))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Content-Disposition", containsString("attachment")));
    }

    @Test
    void 파일이_있으면_위치가_비어도_위치_경고가_없다() throws Exception {
        long id = newDoc();
        // 위치 없음 → 경고 있음
        assertThat(fetch(BASE, id).get("warnings").toString()).contains("위치가 비어");
        upload(id, "a.pdf", "x".getBytes()).andExpect(status().isCreated());
        assertThat(fetch(BASE, id).get("warnings").toString()).doesNotContain("위치가 비어");
    }

    @Test
    void 목록에도_파일_정보가_포함된다() throws Exception {
        long id = newDoc();
        upload(id, "a.pdf", "x".getBytes()).andExpect(status().isCreated());
        JsonNode list = read(mvc.perform(get(BASE + "?size=200")).andExpect(status().isOk())).at("/data/content");
        boolean found = false;
        for (JsonNode d : list) {
            if (d.get("id").asLong() == id) {
                found = true;
                assertThat(d.get("files")).hasSize(1);
            } else {
                assertThat(d.get("files")).isNotNull();
            }
        }
        assertThat(found).isTrue();
    }

    @Test
    void 같은_내용은_409_빈_파일_실행_파일은_400() throws Exception {
        long id = newDoc();
        upload(id, "a.pdf", "same".getBytes()).andExpect(status().isCreated());
        upload(id, "b.pdf", "same".getBytes()).andExpect(status().isConflict());
        upload(id, "empty.pdf", new byte[0]).andExpect(status().isBadRequest());
        upload(id, "run.EXE", "MZ".getBytes()).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message", containsString(".exe")));
        assertThat(fetch(BASE, id).get("files")).hasSize(1);
    }

    @Test
    void 없는_산출물_파트_누락은_각각_404_400() throws Exception {
        upload(999_999L, "a.pdf", "x".getBytes()).andExpect(status().isNotFound());
        long id = newDoc();
        mvc.perform(multipart(BASE + "/{id}/files", id)).andExpect(status().isBadRequest());
    }

    @Test
    void 삭제하면_목록에서_사라지고_다운로드는_404() throws Exception {
        long id = newDoc();
        upload(id, "a.pdf", "x".getBytes()).andExpect(status().isCreated());
        String url = fetch(BASE, id).get("files").get(0).get("downloadUrl").asText();
        mvc.perform(delete(url)).andExpect(status().isNoContent());
        assertThat(fetch(BASE, id).get("files")).isEmpty();
        mvc.perform(get(url)).andExpect(status().isNotFound());
        mvc.perform(delete(url)).andExpect(status().isNotFound());
    }

    @Test
    void 다른_산출물의_파일_id_로는_받을_수_없다() throws Exception {
        long a = newDoc();
        long b = newDoc();
        upload(a, "a.pdf", "x".getBytes()).andExpect(status().isCreated());
        long fileId = fetch(BASE, a).get("files").get(0).get("id").asLong();
        mvc.perform(get(BASE + "/{id}/files/{fid}", b, fileId)).andExpect(status().isNotFound());
        mvc.perform(delete(BASE + "/{id}/files/{fid}", b, fileId)).andExpect(status().isNotFound());
    }

    @Test
    void 산출물을_삭제하면_첨부도_함께_삭제된다() throws Exception {
        long id = newDoc();
        upload(id, "a.pdf", "x".getBytes()).andExpect(status().isCreated());
        mvc.perform(delete(BASE + "/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get(BASE + "/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void 파일명은_경로와_제어문자를_제거한다() {
        assertThat(SupplyDocFileService.sanitizeName("../../etc/passwd")).isEqualTo("passwd");
        assertThat(SupplyDocFileService.sanitizeName("C:\\temp\\보고서.docx")).isEqualTo("보고서.docx");
        assertThat(SupplyDocFileService.sanitizeName("a\u0000b\n.txt")).isEqualTo("ab.txt");
        assertThat(SupplyDocFileService.sanitizeName("..")).isEqualTo("unnamed");
        assertThat(SupplyDocFileService.sanitizeName(null)).isEqualTo("unnamed");
        assertThat(SupplyDocFileService.extensionOf("Archive.TAR.GZ")).isEqualTo("gz");
    }
}
