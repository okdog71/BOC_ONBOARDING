package com.grovesoft.boctracker.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

/** 통합 규칙 테스트 공통 도구: MockMvc 로 JSON 요청을 보내고 응답에서 값을 꺼낸다. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class ApiTestSupport {

    @Autowired
    protected MockMvc mvc;
    @Autowired
    protected ObjectMapper om;

    protected ResultActions send(MockHttpServletRequestBuilder b, Object json) throws Exception {
        return mvc.perform(b.contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(json)));
    }

    protected JsonNode read(ResultActions r) throws Exception {
        return om.readTree(r.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    /** POST 로 만들고(201 기대) 응답 data 를 돌려준다 */
    protected JsonNode create(String path, Map<String, Object> body) throws Exception {
        return read(send(post(path), body).andExpect(status().isCreated())).get("data");
    }

    protected JsonNode fetch(String path, long id) throws Exception {
        return read(mvc.perform(get(path + "/{id}", id)).andExpect(status().isOk())).get("data");
    }

    protected ResultActions setStatus(String path, long id, String status) throws Exception {
        return send(patch(path + "/{id}/status", id), Map.of("status", status));
    }

    protected static Map<String, Object> map(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put((String) kv[i], kv[i + 1]);
        }
        return m;
    }
}
