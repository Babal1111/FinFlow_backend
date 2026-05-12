package com.finflow.admin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;
import java.util.List;

@FeignClient(name = "auth-service")
public interface AuthServiceClient {

    @GetMapping("/auth/users/{id}")
    Map<String, Object> getUserById(@PathVariable("id") Long id);

    @GetMapping("/auth/users/all")
    List<Map<String, Object>> getAllUsers();
}
