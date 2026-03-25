package com.example.finflow.auth_service.config;

import jakarta.servlet.ServletException;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;


@Configuration
public class AppConfig {
    @Bean
    public ModelMapper modelMapper(){
        return  new ModelMapper();
    }

}
