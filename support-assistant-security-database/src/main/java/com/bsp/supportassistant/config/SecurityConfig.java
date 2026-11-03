package com.bsp.supportassistant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// The rules: which URL needs which login.
// Who the users are is in DatabaseUserDetailsService, which reads the table DESK_USERS.
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Read from the top. The first rule that matches the request is the one that applies.
                .authorizeHttpRequests(rules -> rules
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/tickets").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.PATCH, "/api/tickets/*/status").hasRole("AGENT")
                        .anyRequest().authenticated())
                // Ask for a user name and a password in the Authorization header of every request.
                .httpBasic(Customizer.withDefaults())
                // CSRF protection guards pages that log in with a cookie. The clients of this API send no cookie,
                // and with it on every POST and PATCH is refused.
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    // Passwords are stored as BCrypt hashes, never as plain text.
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
