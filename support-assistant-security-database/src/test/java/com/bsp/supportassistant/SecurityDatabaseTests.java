package com.bsp.supportassistant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// These tests need Oracle, and the rows made by db/oracle/02-sample-users.sql.
// They read DESK_USERS and change nothing, so they need no @Transactional.
@SpringBootTest
@AutoConfigureMockMvc
class SecurityDatabaseTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void aUserFromTheTableCanLogIn() throws Exception {
        mockMvc.perform(get("/api/me").with(httpBasic("meera", "meera123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("meera"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_EMPLOYEE"));
    }

    @Test
    void wrongPasswordIs401() throws Exception {
        mockMvc.perform(get("/api/me").with(httpBasic("meera", "wrong")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void aUserThatIsNotInTheTableIs401() throws Exception {
        mockMvc.perform(get("/api/me").with(httpBasic("nobody", "meera123")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void theRoleComesFromTheTable() throws Exception {
        String body = "{\"status\":\"IN_PROGRESS\"}";

        mockMvc.perform(patch("/api/tickets/1/status").with(httpBasic("meera", "meera123"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/tickets/1/status").with(httpBasic("karan", "karan123"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
    }
}
