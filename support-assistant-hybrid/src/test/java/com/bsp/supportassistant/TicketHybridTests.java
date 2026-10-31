package com.bsp.supportassistant;

import com.bsp.supportassistant.dto.CommentRequest;
import com.bsp.supportassistant.dto.CommentResponse;
import com.bsp.supportassistant.dto.CreateTicketRequest;
import com.bsp.supportassistant.dto.TicketResponse;
import com.bsp.supportassistant.dto.TicketWithComments;
import com.bsp.supportassistant.exception.TicketNotFoundException;
import com.bsp.supportassistant.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// These tests use the real package and the real tables in your Oracle database.
// @Transactional on a test class rolls each test back, so the tables stay as they were.
@SpringBootTest
@Transactional
class TicketHybridTests {

    @Autowired
    private TicketService ticketService;

    @Test
    void aTicketCreatedByThePackageIsReadByJpa() {
        long id = ticketService.create(new CreateTicketRequest("Printer is jammed", null, "HIGH"));

        TicketWithComments result = ticketService.get(id);

        assertThat(result.ticket().title()).isEqualTo("Printer is jammed");
        assertThat(result.ticket().status()).isEqualTo("OPEN");
        assertThat(result.comments()).isEmpty();
    }

    @Test
    void commentsAddedByThePackageComeBackInOrder() {
        long id = ticketService.create(new CreateTicketRequest("VPN drops every hour", null, "MEDIUM"));
        ticketService.addComment(id, new CommentRequest("Meera", "It drops at 10:00"));
        ticketService.addComment(id, new CommentRequest("Karan", "Looking now"));

        TicketWithComments result = ticketService.get(id);

        assertThat(result.comments()).extracting(CommentResponse::author).containsExactly("Meera", "Karan");
    }

    @Test
    void changeStatusReturnsTheNewStatus() {
        long id = ticketService.create(new CreateTicketRequest("Cannot log in", null, "LOW"));

        TicketResponse changed = ticketService.changeStatus(id, "IN_PROGRESS");

        assertThat(changed.status()).isEqualTo("IN_PROGRESS");
    }

    @Test
    void thePackageRefusesAStepThatIsNotAllowed() {
        long id = ticketService.create(new CreateTicketRequest("Broken keyboard", null, "LOW"));

        // OPEN -> CLOSED skips two steps. The rule is in the package, so the package says no.
        assertThatThrownBy(() -> ticketService.changeStatus(id, "CLOSED"))
                .isInstanceOf(UncategorizedSQLException.class);
    }

    @Test
    void findByStatusUsesJpa() {
        long id = ticketService.create(new CreateTicketRequest("Slow laptop", null, "LOW"));

        assertThat(ticketService.find("OPEN")).extracting(TicketResponse::id).contains(id);
        assertThat(ticketService.find("CLOSED")).extracting(TicketResponse::id).doesNotContain(id);
    }

    @Test
    void aMissingTicketIsNotFound() {
        assertThatThrownBy(() -> ticketService.get(-1))
                .isInstanceOf(TicketNotFoundException.class);
    }
}
