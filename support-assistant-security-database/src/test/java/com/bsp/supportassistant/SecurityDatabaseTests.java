package com.bsp.supportassistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// These tests need Oracle, and the rows made by db/oracle/02-sample-users.sql.
// They read DESK_USERS and change nothing, so they need no @Transactional.
// The secret is set here, so the tests do not need the JWT_SECRET variable.
@SpringBootTest(properties = "app.jwt.secret=a-test-secret-that-is-at-least-32-characters-long")
@AutoConfigureMockMvc
class SecurityDatabaseTests {

    @Autowired
    private MockMvc mockMvc;

    // Logs in and returns the token from the answer.
    private String login(String username, String password) throws Exception {
        String body = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
        String json = mockMvc.perform(post("/api/public/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return new ObjectMapper().readTree(json).get("token").asText();
    }

    @Test
    void aUserFromTheTableCanLogInAndUseTheToken() throws Exception {
        String token = login("meera", "meera123");

        mockMvc.perform(get("/api/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("meera"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_EMPLOYEE"));
    }

    @Test
    void wrongPasswordIs401() throws Exception {
        mockMvc.perform(post("/api/public/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"meera\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void aUserThatIsNotInTheTableIs401() throws Exception {
        mockMvc.perform(post("/api/public/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nobody\",\"password\":\"meera123\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void noTokenIs401() throws Exception {
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void aWrongTokenIs401() throws Exception {
        mockMvc.perform(get("/api/tickets").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void theRoleComesFromTheTable() throws Exception {
        String body = "{\"status\":\"IN_PROGRESS\"}";
        String meera = login("meera", "meera123");
        String karan = login("karan", "karan123");

        mockMvc.perform(patch("/api/tickets/1/status").header("Authorization", "Bearer " + meera)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/tickets/1/status").header("Authorization", "Bearer " + karan)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
    }
}
