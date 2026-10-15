package com.bsp.supportassistant.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="comments")
public class Comment {
    @Id
    private Long id;

}
