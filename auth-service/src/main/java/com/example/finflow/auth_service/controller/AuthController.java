package com.example.finflow.auth_service.controller;

import com.example.finflow.auth_service.entity.User;
import com.example.finflow.auth_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public String register(@RequestBody User user) {
        authService.register(user);
        return "userCreated";
    }
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return authService.getAllUsers();
    }


    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        return authService.getUserById(id);
    }


    @GetMapping("/users/email")
    public User getByEmail(@RequestParam String email) {
        return authService.getUserByEmail(email);
    }

    @PutMapping("/users/{id}")
    public User update(@PathVariable Long id, @RequestBody User user) {
        return authService.updateUser(id, user);
    }


    @DeleteMapping("/users/{id}")
    public String delete(@PathVariable Long id) {
        authService.deleteUser(id);
        return "Deleted successfully";
    }
}
