package com.bsp.supportassistant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.bsp.supportassistant.security.JwtFilter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// The rules: which URL needs which login.
// Who the users are is in DatabaseUserDetailsService, which reads the table DESK_USERS.
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        http
                // Use the rules in CorsConfig. The browser asks "may I call this?" (an OPTIONS request) before it sends
                // the token, so this must run before the login check. Without it that question gets 401.
                .cors(Customizer.withDefaults())
                // Read from the top. The first rule that matches the request is the one that applies.
                .authorizeHttpRequests(rules -> rules
                        .requestMatchers("/api/public/**").permitAll()
                        // When a request is refused (403) or fails (400, 404), the server forwards to /error to write the answer.
                        // That forward is not logged in again, so without this line a 403 would turn into a 401.
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/tickets").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.PATCH, "/api/tickets/*/status").hasRole("AGENT")
                        .anyRequest().authenticated())
                // Check the token before the URL rules are applied.
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                // No login session. Every request must bring its own token.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Not logged in, or a wrong login: answer 401.
                .exceptionHandling(errors -> errors.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                // CSRF protection guards pages that log in with a cookie. The clients of this API send no cookie,
                // and with it on every POST and PATCH is refused.
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    // Used by the login URL to check a user name and a password.
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // Passwords are stored as BCrypt hashes, never as plain text.
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
