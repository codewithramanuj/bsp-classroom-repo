package com.bsp.supportassistant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Calls the real endpoints on the real H2 database. @Transactional rolls each test back.
@SpringBootTest
@Transactional
class TicketApiTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void sampleTicketsAreThere() throws Exception {
        mvc.perform(get("/api/v1/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("OPEN"))
                .andExpect(jsonPath("$[0].createdAt").exists())
                .andExpect(jsonPath("$[0].dueBy").exists());
    }

    @Test
    void createGivesBackTheIdAndTheTicketStartsOpen() throws Exception {
        long id = create("Printer is jammed", "HIGH");

        mvc.perform(get("/api/v1/tickets").param("status", "open"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem((int) id)));
    }

    @Test
    void filtersByStatusAndPriority() throws Exception {
        mvc.perform(get("/api/v1/tickets").param("status", "OPEN").param("priority", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].priority").value("HIGH"));
    }

    @Test
    void openCountGoesUpByOne() throws Exception {
        int before = openCount("MEDIUM");

        create("VPN drops again", "MEDIUM");

        assertEquals(before + 1, openCount("MEDIUM"));
    }

    @Test
    void changeStatusMovesOneStep() throws Exception {
        long id = create("Reset my password", "LOW");

        mvc.perform(patch("/api/v1/tickets/" + id + "/status").contentType(APPLICATION_JSON)
                        .content("{\"status\":\"in_progress\"}"))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/tickets").param("status", "IN_PROGRESS"))
                .andExpect(jsonPath("$[*].id", hasItem((int) id)));
    }

    @Test
    void aTicketCannotSkipAStep() throws Exception {
        long id = create("Reset my password", "LOW");

        mvc.perform(patch("/api/v1/tickets/" + id + "/status").contentType(APPLICATION_JSON)
                        .content("{\"status\":\"CLOSED\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Ticket " + id + " is OPEN. The next status is IN_PROGRESS"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void aClosedTicketCannotChange() throws Exception {
        // Ticket 4 is CLOSED in the sample data.
        mvc.perform(patch("/api/v1/tickets/4/status").contentType(APPLICATION_JSON)
                        .content("{\"status\":\"OPEN\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ticket 4 is CLOSED and cannot change"));
    }

    @Test
    void unknownTicketIsNotFound() throws Exception {
        mvc.perform(patch("/api/v1/tickets/9999/status").contentType(APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ticket 9999 does not exist"));
    }

    @Test
    void badRequestsAreRefused() throws Exception {
        mvc.perform(post("/api/v1/tickets").contentType(APPLICATION_JSON)
                        .content("{\"description\":\"no title\",\"priority\":\"HIGH\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Title is required"));

        mvc.perform(post("/api/v1/tickets").contentType(APPLICATION_JSON)
                        .content("{\"title\":\"x\",\"priority\":\"URGENT\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Priority must be HIGH, MEDIUM or LOW"));

        mvc.perform(post("/api/v1/tickets/bulk-status").contentType(APPLICATION_JSON)
                        .content("{\"ticketIds\":[],\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/api/v1/tickets").contentType(APPLICATION_JSON).content("not json"))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/api/v1/tickets/open-count"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(startsWith("The parameter 'priority'")));
    }

    @Test
    void bulkChangeMovesEveryTicket() throws Exception {
        long a = create("First", "LOW");
        long b = create("Second", "LOW");

        mvc.perform(post("/api/v1/tickets/bulk-status").contentType(APPLICATION_JSON)
                        .content("{\"ticketIds\":[" + a + "," + b + "],\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/tickets").param("status", "IN_PROGRESS"))
                .andExpect(jsonPath("$[*].id", hasItem((int) a)))
                .andExpect(jsonPath("$[*].id", hasItem((int) b)));
    }

    private long create(String title, String priority) throws Exception {
        String body = "{\"title\":\"" + title + "\",\"priority\":\"" + priority + "\"}";
        MvcResult result = mvc.perform(post("/api/v1/tickets").contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn();
        // The answer looks like {"id":7}.
        String answer = result.getResponse().getContentAsString();
        return Long.parseLong(answer.replaceAll("[^0-9]", ""));
    }

    private int openCount(String priority) throws Exception {
        String text = mvc.perform(get("/api/v1/tickets/open-count").param("priority", priority))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return Integer.parseInt(text);
    }
}
