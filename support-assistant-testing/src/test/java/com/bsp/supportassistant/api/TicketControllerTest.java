package com.bsp.supportassistant.api;

import com.bsp.supportassistant.dto.TicketResponse;
import com.bsp.supportassistant.exception.TicketNotFoundException;
import com.bsp.supportassistant.service.TicketService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.verification.Times;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Test 3: MockMvc. It sends a pretend HTTP request to the controller, with no real server.
// @WebMvcTest starts only the web layer. The service is a mock.
@WebMvcTest(TicketController.class)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService ticketService;

    @Test
    void postCreatesATicketAndAnswers201() throws Exception {
        when(ticketService.create(any()))
                .thenReturn(new TicketResponse(1L, "Printer is jammed", "HIGH", "OPEN"));

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Printer is jammed","priority":"HIGH"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("OPEN"));
        Mockito.verify(ticketService, Mockito.times(1)).create(any());
    }



}
