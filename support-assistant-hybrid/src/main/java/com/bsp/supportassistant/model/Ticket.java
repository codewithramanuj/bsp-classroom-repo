package com.bsp.supportassistant.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;
import java.util.List;

// One row of DESK_TICKETS. @Immutable: JPA reads this table but never writes it.
// The PL/SQL package writes it, so every rule is checked.
@Entity
@Immutable
@Table(name = "DESK_TICKETS")
public class Ticket {

    @Id
    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private LocalDateTime createdAt;
    private LocalDateTime dueBy;

    // The relationship: DESK_COMMENTS.TICKET_ID points to this ticket.
    // The comments are read only when getComments() is called.
    @OneToMany
    @JoinColumn(name = "TICKET_ID")
    @OrderBy("id")
    private List<Comment> comments;

    protected Ticket() {
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getPriority() {
        return priority;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getDueBy() {
        return dueBy;
    }

    public List<Comment> getComments() {
        return comments;
    }
}
