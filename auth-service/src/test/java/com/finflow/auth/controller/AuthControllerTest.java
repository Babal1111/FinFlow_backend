package com.finflow.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finflow.auth.dto.AuthResponse;
import com.finflow.auth.dto.LoginRequest;
import com.finflow.auth.dto.SignupRequest;
import com.finflow.auth.service.AuthService;
import com.finflow.auth.security.JwtAuthFilter;
import com.finflow.auth.security.UserDetailsServiceImpl;
import com.finflow.auth.security.JwtUtil;
import com.finflow.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        signupRequest = new SignupRequest();
        signupRequest.setName("John");
        signupRequest.setEmail("john@example.com");
        signupRequest.setPassword("pass123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john@example.com");
        loginRequest.setPassword("pass123");

        authResponse = new AuthResponse("token", "APPLICANT", 1L);
    }

    @Test
    @DisplayName("POST /auth/signup: should return 201 Created")
    void signup_shouldReturnCreated() throws Exception {
        when(authService.signup(any())).thenReturn(authResponse);

        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("token"))
                .andExpect(jsonPath("$.userId").value(1L));
    }

    @Test
    @DisplayName("POST /auth/login: should return 200 OK")
    void login_shouldReturnOk() throws Exception {
        when(authService.login(any())).thenReturn(authResponse);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token"));
    }

    @Test
    @DisplayName("GET /auth/users/{id}: should return user map")
    void getUserById_shouldReturnUser() throws Exception {
        Map<String, Object> userMap = Map.of("id", 1L, "email", "john@example.com");
        when(authService.getUserById(1L)).thenReturn(userMap);

        mockMvc.perform(get("/auth/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    @DisplayName("GET /auth/users/all: should return list of users")
    void getAllUsers_shouldReturnList() throws Exception {
        List<Map<String, Object>> users = List.of(Map.of("id", 1L, "email", "john@example.com"));
        when(authService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/auth/users/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("john@example.com"));
    }

    @Test
    @DisplayName("GET /auth/test: should return simple string")
    void test_shouldReturnString() throws Exception {
        mockMvc.perform(get("/auth/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("JWT working"));
    }
}
