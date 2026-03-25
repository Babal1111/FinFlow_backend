package com.example.finflow.auth_service.controller;

import com.example.finflow.auth_service.dto.AuthResponseDto;
import com.example.finflow.auth_service.dto.LoginRequestDto;
import com.example.finflow.auth_service.dto.RegisterRequestDto;
import com.example.finflow.auth_service.dto.UserResponseDto;
import com.example.finflow.auth_service.entity.User;
import com.example.finflow.auth_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    //reqArgs const only works with final feilds
    //Lombok generates constructor --> Spring injects bean

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@RequestBody RegisterRequestDto req) {
        return ResponseEntity.ok(authService.register(req));
    }
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginRequestDto req){
        return ResponseEntity.ok(authService.login(req));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getAll")
    public ResponseEntity<List<UserResponseDto>>getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(authService.getUserById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/email")
    public ResponseEntity<UserResponseDto> getByEmail(@RequestParam String email) {
        return ResponseEntity.ok(authService.getUserByEmail(email));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserResponseDto> update(@PathVariable Long id, @RequestBody User user) {
        return ResponseEntity.ok(authService.updateUser(id, user));
    }


    @DeleteMapping("/users/{id}")
    public String delete(@PathVariable Long id) {
        authService.deleteUser(id);
        return "Deleted successfully";
    }
}
