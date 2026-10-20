package com.grovesoft.boctracker.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grovesoft.boctracker.interfacelink.InterfaceLinkRepository;
import com.grovesoft.boctracker.user.AppUser;
import com.grovesoft.boctracker.user.UserRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
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

/** 로그인·세션 보호·감사 컬럼(등록자/수정자/삭제 이력)·사용자 관리 통합 검증(로그인 검사를 켠 상태). */
@SpringBootTest(properties = {"app.auth.enabled=true", "app.auth.bootstrap-users=false"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthAuditApiTest {

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
    void seedUsers() {
        cleanup();
        users.save(user("alice", "앨리스"));
        users.save(user("bobby", "바비"));
    }

    @AfterEach
    void cleanup() {
        links.deleteAll();
        users.deleteAll();
        jdbc.update("DELETE FROM audit_log");
    }

    private AppUser user(String loginId, String name) {
        return AppUser.builder().loginId(loginId).passwordHash(encoder.encode(loginId)).mustChangePassword(true)
                .affiliation("grove").name(name).build();
    }

    private MockHttpSession login(String id, String pw) throws Exception {
        MvcResult r = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(Map.of("loginId", id, "password", pw))))
                .andExpect(status().isOk()).andReturn();
        return (MockHttpSession) r.getRequest().getSession(false);
    }

    private JsonNode json(MvcResult r) throws Exception {
        return om.readTree(r.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private String linkBody(String system) throws Exception {
        return om.writeValueAsString(Map.of("systemName", system));
    }

    @Test
    void 로그인_없이는_api가_401이고_로그인_API는_열려있다() throws Exception {
        mvc.perform(get("/api/interface-links")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"loginId\":\"alice\",\"password\":\"x\"}")).andExpect(status().isUnauthorized());
    }

    @Test
    void 로그인하면_사용할_수_있고_응답에_비밀번호가_없다_로그아웃하면_다시_401() throws Exception {
        MockHttpSession s = login("alice", "alice");
        MvcResult me = mvc.perform(get("/api/auth/me").session(s)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.loginId").value("alice"))
                .andExpect(jsonPath("$.data.mustChangePassword").value(true)).andReturn();
        assertThat(me.getResponse().getContentAsString()).doesNotContain("password").doesNotContain("$2a$");
        mvc.perform(get("/api/interface-links").session(s)).andExpect(status().isOk());

        mvc.perform(post("/api/auth/logout").session(s)).andExpect(status().isNoContent());
        mvc.perform(get("/api/interface-links").session(s)).andExpect(status().isUnauthorized());
    }

    @Test
    void 비밀번호_5회_연속_실패하면_잠긴다() throws Exception {
        for (int i = 0; i < 5; i++) {
            mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"loginId\":\"bobby\",\"password\":\"bad\"}")).andExpect(status().isUnauthorized());
        }
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"loginId\":\"bobby\",\"password\":\"bobby\"}")).andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error.code").value("TOO_MANY_ATTEMPTS"));
    }

    @Test
    void 등록자_수정자가_기록되고_삭제는_이력에_남는다() throws Exception {
        MockHttpSession alice = login("alice", "alice");
        MvcResult created = mvc.perform(post("/api/interface-links").session(alice)
                .contentType(MediaType.APPLICATION_JSON).content(linkBody("감사-시스템"))).andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.createdBy").value("alice"))
                .andExpect(jsonPath("$.data.updatedBy").value("alice")).andReturn();
        long id = json(created).at("/data/id").asLong();

        MockHttpSession bobby = login("bobby", "bobby");
        mvc.perform(put("/api/interface-links/" + id).session(bobby).contentType(MediaType.APPLICATION_JSON)
                .content(linkBody("감사-시스템-수정"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.createdBy").value("alice"))
                .andExpect(jsonPath("$.data.updatedBy").value("bobby"));

        mvc.perform(delete("/api/interface-links/" + id).session(bobby)).andExpect(status().isNoContent());
        List<Map<String, Object>> log = jdbc.queryForList(
                "SELECT entity_type, entity_id, action, actor FROM audit_log WHERE entity_id = ?", id);
        assertThat(log).hasSize(1);
        assertThat(log.get(0)).containsEntry("ACTION", "DELETE").containsEntry("ACTOR", "bobby")
                .containsEntry("ENTITY_TYPE", "InterfaceLink");
    }

    @Test
    void 비밀번호_변경_규칙() throws Exception {
        MockHttpSession s = login("alice", "alice");
        String bad = om.writeValueAsString(Map.of("currentPassword", "틀림", "newPassword", "newpass1"));
        mvc.perform(put("/api/auth/password").session(s).contentType(MediaType.APPLICATION_JSON).content(bad))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.code").value("BUSINESS_RULE"));
        String same = om.writeValueAsString(Map.of("currentPassword", "alice", "newPassword", "alice"));
        mvc.perform(put("/api/auth/password").session(s).contentType(MediaType.APPLICATION_JSON).content(same))
                .andExpect(status().isBadRequest());
        String ok = om.writeValueAsString(Map.of("currentPassword", "alice", "newPassword", "newpass1"));
        mvc.perform(put("/api/auth/password").session(s).contentType(MediaType.APPLICATION_JSON).content(ok))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.mustChangePassword").value(false));

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"loginId\":\"alice\",\"password\":\"alice\"}")).andExpect(status().isUnauthorized());
        login("alice", "newpass1");
    }

    @Test
    void 사용자_관리_등록_수정_초기화_삭제() throws Exception {
        MockHttpSession s = login("alice", "alice");
        String body = om.writeValueAsString(Map.of("loginId", "carol", "affiliation", "grove", "name", "캐롤"));
        MvcResult created = mvc.perform(post("/api/users").session(s).contentType(MediaType.APPLICATION_JSON)
                .content(body)).andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.createdBy").value("alice")).andReturn();
        long id = json(created).at("/data/id").asLong();
        mvc.perform(post("/api/users").session(s).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
        login("carol", "carol"); // 초기 비밀번호 = 로그인 ID

        String renamed = om.writeValueAsString(Map.of("loginId", "carol2", "affiliation", "grove", "name", "캐롤"));
        mvc.perform(put("/api/users/" + id).session(s).contentType(MediaType.APPLICATION_JSON).content(renamed))
                .andExpect(status().isBadRequest());
        String edit = om.writeValueAsString(Map.of("loginId", "carol", "affiliation", "grove(NIT)", "name", "캐롤2"));
        mvc.perform(put("/api/users/" + id).session(s).contentType(MediaType.APPLICATION_JSON).content(edit))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.affiliation").value("grove(NIT)"))
                .andExpect(jsonPath("$.data.updatedBy").value("alice"));
        String badDates = om.writeValueAsString(Map.of("loginId", "carol", "affiliation", "grove", "name", "캐롤",
                "joinDate", "2026-09-10", "leaveDate", "2026-09-01"));
        mvc.perform(put("/api/users/" + id).session(s).contentType(MediaType.APPLICATION_JSON).content(badDates))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/api/users/" + id + "/password-reset").session(s)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mustChangePassword").value(true));

        long selfId = users.findByLoginId("alice").orElseThrow().getId();
        mvc.perform(delete("/api/users/" + selfId).session(s)).andExpect(status().isBadRequest());
        mvc.perform(delete("/api/users/" + id).session(s)).andExpect(status().isNoContent());
        mvc.perform(get("/api/users").session(s)).andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(2));
    }
}
