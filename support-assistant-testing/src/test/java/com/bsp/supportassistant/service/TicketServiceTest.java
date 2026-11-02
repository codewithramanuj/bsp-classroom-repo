package com.bsp.supportassistant.service;

import com.bsp.supportassistant.dto.CreateTicketRequest;
import com.bsp.supportassistant.dto.TicketResponse;
import com.bsp.supportassistant.exception.InvalidStatusChangeException;
import com.bsp.supportassistant.exception.TicketNotFoundException;
import com.bsp.supportassistant.model.Ticket;
import com.bsp.supportassistant.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketService ticketService;

    @Test
    void createWithoutATitleSavesNothing() {
        assertThrows(IllegalArgumentException.class, ()->ticketService.create(new CreateTicketRequest("","HIGH")));

    }

    @Test
    void createSavesANewTicketAsOpen() {
        // Arrange: tell the mock what to answer when save is called.
        when(ticketRepository.save(any(Ticket.class)))
                .thenReturn(new Ticket(1L, "Printer is jammed", "HIGH", "OPEN"));


        // Act
        TicketResponse response = ticketService.create(new CreateTicketRequest("Printer is jammed", "HIGH"));

        // Assert: the answer, and what the service gave to save.
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo("OPEN");


    }
}
