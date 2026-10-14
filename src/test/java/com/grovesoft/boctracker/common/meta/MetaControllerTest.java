package com.grovesoft.boctracker.common.meta;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MetaControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    void enums_조회() throws Exception {
        mvc.perform(get("/api/meta/enums"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.milestoneStatus[0]").value("미착수"))
                .andExpect(jsonPath("$.data.handoverStatus[1]").value("예정"))
                .andExpect(jsonPath("$.data.week.length()").value(4))
                .andExpect(jsonPath("$.data.generation[4]").value("-"))
                .andExpect(jsonPath("$.data.devCheck[1]").value("dev미노출"));
    }
}
