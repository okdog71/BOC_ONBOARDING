package com.grovesoft.boctracker.auditlog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grovesoft.boctracker.interfacelink.InterfaceLinkRepository;
import com.grovesoft.boctracker.user.AppUser;
import com.grovesoft.boctracker.user.UserRepository;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/** 등록·수정·삭제가 변경 이력에 6하 항목으로 남고, 필터로 조회되는지 검증. */
@SpringBootTest(properties = {"app.auth.enabled=true", "app.auth.bootstrap-users=false"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuditLogApiTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;
    @Autowired
    UserRepository users;
    @Autowired
    InterfaceLinkRepository links;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        cleanup();
        for (String[] u : new String[][] {{"alice", "앨리스"}, {"bobby", "바비"}}) {
            users.save(AppUser.builder().loginId(u[0]).passwordHash(encoder.encode(u[0])).mustChangePassword(true)
                    .affiliation("grove").name(u[1]).build());
        }
    }

    @AfterEach
    void cleanup() {
        links.deleteAll();
        users.deleteAll();
        jdbc.update("DELETE FROM audit_log");
    }

    private MockHttpSession login(String id) throws Exception {
        MvcResult r = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(Map.of("loginId", id, "password", id)))).andExpect(status().isOk()).andReturn();
        return (MockHttpSession) r.getRequest().getSession(false);
    }

    private JsonNode json(MvcResult r) throws Exception {
        return om.readTree(r.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private JsonNode logs(MockHttpSession s, String query) throws Exception {
        return json(mvc.perform(get("/api/audit-logs" + query).session(s)).andExpect(status().isOk()).andReturn()).at("/data/content");
    }

    @Test
    void 등록_수정_삭제가_6하_항목으로_남고_필터로_조회된다() throws Exception {
        MockHttpSession alice = login("alice");
        MvcResult created = mvc.perform(post("/api/interface-links").session(alice).contentType(MediaType.APPLICATION_JSON)
                .with(r -> { r.setRemoteAddr("10.1.2.3"); return r; })
                .content(om.writeValueAsString(Map.of("systemName", "이력-시스템", "auth", "API Key"))))
                .andExpect(status().isCreated()).andReturn();
        long id = json(created).at("/data/id").asLong();

        MockHttpSession bobby = login("bobby");
        mvc.perform(put("/api/interface-links/" + id).session(bobby).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(Map.of("systemName", "이력-시스템", "auth", "OAuth")))).andExpect(status().isOk());
        mvc.perform(delete("/api/interface-links/" + id).session(bobby)).andExpect(status().isNoContent());

        JsonNode all = logs(alice, "?domain=연동 인터페이스");
        assertThat(all).hasSize(3);
        // 최신순: DELETE, UPDATE, CREATE
        assertThat(all.get(0).get("action").asText()).isEqualTo("DELETE");
        JsonNode update = all.get(1);
        assertThat(update.get("action").asText()).isEqualTo("UPDATE");
        assertThat(update.get("actor").asText()).isEqualTo("bobby");
        assertThat(update.get("actorName").asText()).isEqualTo("바비");
        assertThat(update.get("targetLabel").asText()).isEqualTo("이력-시스템");
        assertThat(update.get("entityId").asLong()).isEqualTo(id);
        assertThat(update.get("request").asText()).isEqualTo("PUT /api/interface-links/" + id);
        assertThat(update.get("changes")).hasSize(1); // 바뀐 필드만
        assertThat(update.at("/changes/0/field").asText()).isEqualTo("auth");
        assertThat(update.at("/changes/0/from").asText()).isEqualTo("API Key");
        assertThat(update.at("/changes/0/to").asText()).isEqualTo("OAuth");
        JsonNode create = all.get(2);
        assertThat(create.get("actor").asText()).isEqualTo("alice");
        assertThat(create.get("clientIp").asText()).isEqualTo("10.1.2.3");
        assertThat(create.get("changes").toString()).contains("systemName").contains("API Key");

        assertThat(logs(alice, "?actor=alice")).extracting(n -> n.get("action").asText()).containsExactly("CREATE");
        assertThat(logs(alice, "?action=UPDATE&domain=연동 인터페이스")).hasSize(1);
        assertThat(logs(alice, "?q=oauth")).hasSize(2); // 변경 내용 검색(수정 후 값, 삭제 직전 값)
        assertThat(logs(alice, "?domain=기술부채")).isEmpty();
        assertThat(logs(alice, "?from=2000-01-01&to=2000-01-02")).isEmpty();
        assertThat(logs(alice, "?ip=10.1.2.3")).hasSize(1);
    }

    @Test
    void 비밀번호_값은_이력에_남지_않고_잘못된_필터는_400() throws Exception {
        MockHttpSession alice = login("alice");
        mvc.perform(post("/api/users").session(alice).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(Map.of("loginId", "carol", "affiliation", "grove", "name", "캐롤"))))
                .andExpect(status().isCreated());
        JsonNode row = logs(alice, "?domain=사용자&action=CREATE").get(0);
        assertThat(row.get("targetCode").asText()).isEqualTo("carol");
        assertThat(row.get("changes").toString()).doesNotContain("$2a$").contains("(변경됨)");

        mvc.perform(get("/api/audit-logs?action=BOGUS").session(alice)).andExpect(status().isBadRequest());
        mvc.perform(get("/api/audit-logs?from=2026-02-01&to=2026-01-01").session(alice)).andExpect(status().isBadRequest());
        mvc.perform(get("/api/audit-logs/domains").session(alice)).andExpect(status().isOk());
    }

    @Test
    void 로그인_없이는_조회할_수_없다() throws Exception {
        mvc.perform(get("/api/audit-logs")).andExpect(status().isUnauthorized());
    }
}
