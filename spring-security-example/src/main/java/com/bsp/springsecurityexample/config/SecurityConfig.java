package com.bsp.springsecurityexample.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain configure(HttpSecurity http){
      return   http.authorizeHttpRequests(rules ->
              rules.requestMatchers("/admin/**").hasRole("ADMIN")
                      .requestMatchers("/user/**").hasAnyRole("ADMIN","USER")
                      .requestMatchers("/**").permitAll()
                      .anyRequest().authenticated()
              )
              .formLogin(Customizer.withDefaults())
              .build();
    }

}
