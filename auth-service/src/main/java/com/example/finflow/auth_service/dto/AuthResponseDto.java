package com.example.finflow.auth_service.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {
    private String name;
    private String email;
    private String token;
    private String role;
    private String message;
}
