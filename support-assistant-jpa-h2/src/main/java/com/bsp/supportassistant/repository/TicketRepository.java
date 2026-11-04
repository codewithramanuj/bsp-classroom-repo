package com.bsp.supportassistant.repository;

import com.bsp.supportassistant.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    long countByPriorityAndStatusIn(String priority, Collection<String> statuses);

    // Both filters are optional: a null value means "any".
    @Query("""
            select t from Ticket t
             where (:status is null or t.status = :status)
               and (:priority is null or t.priority = :priority)
             order by t.id
            """)
    List<Ticket> search(@Param("status") String status, @Param("priority") String priority);
}
