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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// MockMvc sends pretend requests through the whole security filter, with the real users from UsersConfig.
// httpBasic("meera", "meera123") adds the Authorization header, as curl -u does.
@SpringBootTest
@AutoConfigureMockMvc
class SecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicUrlNeedsNoLogin() throws Exception {
        mockMvc.perform(get("/api/public/ping"))
                .andExpect(status().isOk());
    }

    @Test
    void noLoginIs401AndAsksForBasicAuth() throws Exception {
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("WWW-Authenticate"));
    }

    @Test
    void wrongPasswordIs401() throws Exception {
        mockMvc.perform(get("/api/tickets").with(httpBasic("meera", "wrong")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rightPasswordIsLetIn() throws Exception {
        mockMvc.perform(get("/api/me").with(httpBasic("meera", "meera123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("meera"))
                .andExpect(jsonPath("$.roles.length()").value(1))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_EMPLOYEE"));
    }

    @Test
    void anEmployeeCanRaiseATicket() throws Exception {
        mockMvc.perform(post("/api/tickets").with(httpBasic("meera", "meera123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Mouse does not work\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void anAgentCannotRaiseATicketIs403() throws Exception {
        mockMvc.perform(post("/api/tickets").with(httpBasic("karan", "karan123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Mouse does not work\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void onlyAnAgentCanChangeAStatus() throws Exception {
        String body = "{\"status\":\"IN_PROGRESS\"}";

        mockMvc.perform(patch("/api/tickets/1/status").with(httpBasic("meera", "meera123"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/tickets/1/status").with(httpBasic("karan", "karan123"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
    }
}
