package com.finflow.auth.service;

import com.finflow.auth.dto.AuthResponse;
import com.finflow.auth.dto.LoginRequest;
import com.finflow.auth.dto.SignupRequest;
import com.finflow.auth.entity.Role;
import com.finflow.auth.entity.User;
import com.finflow.auth.repository.UserRepository;
import com.finflow.auth.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private ModelMapper modelMapper;

    @InjectMocks
    private AuthService authService;

    private User savedUser;
    private SignupRequest signupRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("John Doe");
        savedUser.setEmail("john@example.com");
        savedUser.setPassword("encoded_password");
        savedUser.setRole(Role.APPLICANT);

        signupRequest = new SignupRequest();
        signupRequest.setName("John Doe");
        signupRequest.setEmail("john@example.com");
        signupRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john@example.com");
        loginRequest.setPassword("password123");
    }

    // ─── signup ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("signup: should register user and return AuthResponse with token")
    void signup_shouldReturnAuthResponse() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(modelMapper.map(signupRequest, User.class)).thenReturn(savedUser);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(savedUser)).thenReturn(savedUser);
        when(jwtUtil.generateAccessToken(savedUser)).thenReturn("access_token");

        AuthResponse result = authService.signup(signupRequest);

        assertNotNull(result);
        assertEquals("access_token", result.getToken());
        assertEquals(1L, result.getUserId());
        assertEquals("APPLICANT", result.getRole());
        verify(userRepository).save(savedUser);
    }

    @Test
    @DisplayName("signup: should throw when email is already registered")
    void signup_shouldThrow_whenEmailExists() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.signup(signupRequest));

        assertEquals("Email already registered", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("signup: should assign ADMIN role when role=ADMIN in request")
    void signup_adminRole_shouldSetAdminRole() {
        signupRequest.setRole("ADMIN");
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(modelMapper.map(signupRequest, User.class)).thenReturn(savedUser);
        when(passwordEncoder.encode(any())).thenReturn("encoded_password");
        when(userRepository.save(savedUser)).thenReturn(savedUser);
        when(jwtUtil.generateAccessToken(savedUser)).thenReturn("access_token");

        authService.signup(signupRequest);

        assertEquals(Role.ADMIN, savedUser.getRole());
    }

    @Test
    @DisplayName("signup: should assign APPLICANT role when role is null")
    void signup_defaultRole_shouldSetApplicantRole() {
        signupRequest.setRole(null);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(modelMapper.map(signupRequest, User.class)).thenReturn(savedUser);
        when(passwordEncoder.encode(any())).thenReturn("encoded_password");
        when(userRepository.save(savedUser)).thenReturn(savedUser);
        when(jwtUtil.generateAccessToken(savedUser)).thenReturn("access_token");

        authService.signup(signupRequest);

        assertEquals(Role.APPLICANT, savedUser.getRole());
    }

    // ─── login ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("login: should authenticate and return AuthResponse with token")
    void login_shouldReturnAuthResponse() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(savedUser));
        when(jwtUtil.generateAccessToken(savedUser)).thenReturn("access_token");

        AuthResponse result = authService.login(loginRequest);

        assertNotNull(result);
        assertEquals("access_token", result.getToken());
        assertEquals(1L, result.getUserId());
        assertEquals("APPLICANT", result.getRole());
    }

    @Test
    @DisplayName("login: should throw when user not found after authentication")
    void login_shouldThrow_whenUserNotFound() {
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(loginRequest));

        assertEquals("User not found", ex.getMessage());
    }
}
