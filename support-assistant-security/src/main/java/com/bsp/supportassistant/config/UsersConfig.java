package com.bsp.supportassistant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

// Who the users are. Here they are two fixed users in memory, made when the application starts.
// These passwords are for the classroom only. Never write a real password in code.
@Configuration
public class UsersConfig {

    @Bean
    UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails meera = User.withUsername("meera")
                .password(passwordEncoder.encode("meera123"))
                .roles("EMPLOYEE")            // stored as the authority ROLE_EMPLOYEE
                .build();
        UserDetails karan = User.withUsername("karan")
                .password(passwordEncoder.encode("karan123"))
                .roles("AGENT")
                .build();
        return new InMemoryUserDetailsManager(meera, karan);
    }
}
