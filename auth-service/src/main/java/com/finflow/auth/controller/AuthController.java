package com.finflow.auth.controller;

import com.finflow.auth.dto.AuthResponse;
import com.finflow.auth.dto.LoginRequest;
import com.finflow.auth.dto.SignupRequest;
import com.finflow.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;



    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(
            @Valid @RequestBody SignupRequest request) {


        log.info("Signup request for email: {}", request.getEmail());
        AuthResponse response = authService.signup(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("Login request for email: {}", request.getEmail());
        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/test")
    public String test() {
        return "JWT working";
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<java.util.Map<String, Object>> getUserById(@PathVariable Long id) {
        log.info("Get user by id: {}", id);
        return ResponseEntity.ok(authService.getUserById(id));
    }

    @GetMapping("/users/all")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<java.util.List<java.util.Map<String, Object>>> getAllUsers(
//            @RequestHeader("X-User-Role") String role
    ) {
//        if(!role.equals("ADMIN")){
//            throw  new RuntimeException("ACESS DENIED");
//        }
        log.info("Get all users request");
        return ResponseEntity.ok(authService.getAllUsers());
    }
}
