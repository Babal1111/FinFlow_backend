package com.finflow.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @Email(message = "Enter valid Email")
    @NotBlank(message = "Email Required")
    private String email;

    @NotBlank(message = "Password Required ")
    private String password;
}