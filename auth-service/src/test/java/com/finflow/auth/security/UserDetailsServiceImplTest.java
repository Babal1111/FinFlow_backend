package com.finflow.auth.security;

import com.finflow.auth.entity.Role;
import com.finflow.auth.entity.User;
import com.finflow.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("test@test.com");
        user.setPassword("pass");
        user.setRole(Role.APPLICANT);
    }

    @Test
    @DisplayName("UserDetailsServiceImpl: should load user by email")
    void loadUserByUsername_Success() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("test@test.com");

        assertNotNull(result);
        assertEquals("test@test.com", result.getUsername());
        assertTrue(result.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_APPLICANT")));
    }

    @Test
    @DisplayName("UserDetailsServiceImpl: should throw when user not found")
    void loadUserByUsername_NotFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("unknown@test.com"));
    }
}
