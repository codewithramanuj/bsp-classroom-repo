package com.bsp.supportassistant;

import com.bsp.supportassistant.dto.CreateTicketRequest;
import com.bsp.supportassistant.dto.TicketResponse;
import com.bsp.supportassistant.exception.StatusChangeNotAllowedException;
import com.bsp.supportassistant.model.Ticket;
import com.bsp.supportassistant.repository.TicketRepository;
import com.bsp.supportassistant.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// The rules of the service, on the real H2 database.
@SpringBootTest
class TicketRulesTests {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketRepository ticketRepository;

    @Test
    @Transactional
    void dueDateFollowsThePriority() {
        assertThat(gapInDays("HIGH")).isEqualTo(1);
        assertThat(gapInDays("MEDIUM")).isEqualTo(3);
        assertThat(gapInDays("LOW")).isEqualTo(7);
    }

    // This test is NOT @Transactional on purpose. A test transaction would wrap the service call,
    // so the bulk change could never roll back by itself. Here the service owns the transaction,
    // and we read the result afterwards. The two tickets it creates stay in the in-memory database.
    @Test
    void aFailedBulkChangeUndoesTheTicketsBeforeIt() {
        long first = ticketService.createTicket(new CreateTicketRequest("First", null, "LOW"));
        long second = ticketService.createTicket(new CreateTicketRequest("Second", null, "LOW"));
        ticketService.changeStatus(second, "IN_PROGRESS");

        // first can move to IN_PROGRESS, but second is already there and must go to RESOLVED.
        assertThatThrownBy(() -> ticketService.changeStatus(List.of(first, second), "IN_PROGRESS"))
                .isInstanceOf(StatusChangeNotAllowedException.class);

        Ticket stillOpen = ticketRepository.findById(first).orElseThrow();
        assertThat(stillOpen.getStatus()).isEqualTo("OPEN");
    }

    private long gapInDays(String priority) {
        long id = ticketService.createTicket(new CreateTicketRequest("Gap " + priority, null, priority));
        TicketResponse ticket = ticketService.findTickets(null, priority).stream()
                .filter(t -> t.id() == id)
                .findFirst()
                .orElseThrow();
        return Duration.between(ticket.createdAt(), ticket.dueBy()).toDays();
    }
}
