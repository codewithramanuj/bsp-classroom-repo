package com.bsp.supportassistant.repository;

import com.bsp.supportassistant.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Spring Data writes the SQL from the method name: select ... from DESK_USERS where USERNAME = ?
public interface UserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);
}
