package com.bsp.supportassistant.service;

import com.bsp.supportassistant.model.AppUser;
import com.bsp.supportassistant.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

// JUnit with Mockito. The repository is a mock, so this test needs no database.
@ExtendWith(MockitoExtension.class)
class DatabaseUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DatabaseUserDetailsService service;

    @Test
    void aKnownUserGetsTheStoredHashAndTheRolePrefix() {
        when(userRepository.findByUsername("karan"))
                .thenReturn(Optional.of(new AppUser(2L, "karan", "a-stored-hash", "AGENT")));

        UserDetails user = service.loadUserByUsername("karan");

        assertThat(user.getUsername()).isEqualTo("karan");
        assertThat(user.getPassword()).isEqualTo("a-stored-hash");
        assertThat(user.getAuthorities()).extracting("authority").containsExactly("ROLE_AGENT");
    }

    @Test
    void anUnknownUserIsRefused() {
        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("nobody"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
