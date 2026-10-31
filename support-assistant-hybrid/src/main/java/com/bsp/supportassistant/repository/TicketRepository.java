package com.bsp.supportassistant.repository;

import com.bsp.supportassistant.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// JPA reads the tickets. Spring Data writes the SQL from the method names.
// Nothing here changes a ticket: that is the job of TicketPackageRepository.
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findAllByOrderById();

    List<Ticket> findByStatusOrderById(String status);
}
