package com.bsp.supportassistant.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

// One row of DESK_COMMENTS. Read only, like Ticket. The package ADD_COMMENT writes it.
@Entity
@Immutable
@Table(name = "DESK_COMMENTS")
public class Comment {

    @Id
    private Long id;
    private String author;
    private String body;
    private LocalDateTime createdAt;

    protected Comment() {
    }

    public Long getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getBody() {
        return body;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
