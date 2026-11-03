package com.bsp.supportassistant.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

// One row of DESK_USERS. @Immutable: the application only reads users, it never changes them.
// The class is called AppUser so that it is not mixed up with Spring Security's own User class.
@Entity
@Immutable
@Table(name = "DESK_USERS")
public class AppUser {

    @Id
    private Long id;
    private String username;
    private String passwordHash;   // the BCrypt hash, never the password
    private String userRole;       // EMPLOYEE or AGENT, without the ROLE_ prefix

    protected AppUser() {
    }

    // Only the tests and the service need to build one by hand.
    public AppUser(Long id, String username, String passwordHash, String userRole) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.userRole = userRole;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getUserRole() {
        return userRole;
    }
}
