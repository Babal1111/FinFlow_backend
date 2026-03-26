package com.finflow.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupRequest {

    @NotBlank(message = "Name is Required")
    private String name;

    @Email(message = "Enter valid Email")
    @NotBlank(message = "Email Required")
    private String email;

    @Size(min = 6, message = "Password is atleast of 6 Character")
    @NotBlank(message = "Password Required ")
    private String password;


    private String role;
}