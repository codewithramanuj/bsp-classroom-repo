package com.bsp.supportassistant.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

// One row of SUPPORT_TICKETS. Status and priority are plain text: OPEN, HIGH and so on.
@Entity
@Table(name = "SUPPORT_TICKETS")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ticket_seq")
    @SequenceGenerator(name = "ticket_seq", sequenceName = "SUPPORT_TICKETS_SEQ", allocationSize = 1)
    private Long id;

    @Column(length = 100)
    private String title;

    @Column(length = 500)
    private String description;

    private String status;

    private String priority;

    private LocalDateTime createdAt;

    private LocalDateTime dueBy;

    // JPA needs a constructor without arguments.
    protected Ticket() {
    }

    // A new ticket always starts OPEN. The service works out the due date from the priority.
    public Ticket(String title, String description, String priority, LocalDateTime createdAt, LocalDateTime dueBy) {
        this.title = title;
        this.description = description;
        this.status = "OPEN";
        this.priority = priority;
        this.createdAt = createdAt;
        this.dueBy = dueBy;
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

    public void setStatus(String status) {
        this.status = status;
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
}
