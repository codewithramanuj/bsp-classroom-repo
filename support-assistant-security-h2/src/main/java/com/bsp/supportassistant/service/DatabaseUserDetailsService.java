package com.bsp.supportassistant.service;

import com.bsp.supportassistant.model.AppUser;
import com.bsp.supportassistant.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// Spring Security calls loadUserByUsername with the name from the Basic header.
// This class looks the user up in the table. It does not check the password:
// Spring Security compares the typed password with the hash we return.
@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public DatabaseUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        AppUser appUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user named " + username));

        return User.withUsername(appUser.getUsername())
                .password(appUser.getPasswordHash())
                .roles(appUser.getUserRole())     // EMPLOYEE becomes the authority ROLE_EMPLOYEE
                .build();
    }
}
