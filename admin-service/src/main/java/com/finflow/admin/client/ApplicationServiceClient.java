package com.finflow.admin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Feign Client for application-service.
 * Service name must match the registered Eureka service name.
 */
@FeignClient(name = "application-service")
public interface ApplicationServiceClient {

    /**
     * Fetch all non-draft applications.
     * X-User-Role: ADMIN is required by the application-service endpoint.
     */
//    @GetMapping("/applications/all")
//    List<?> getAllApplications(@RequestHeader("X-User-Role") String role);

    @GetMapping("/applications/all")
    Page<?> getAllApplications(
            @RequestHeader("X-User-Role") String role,
            @RequestParam("page") int page,
            @RequestParam("size") int size
    );
    /**
     * Update the status of a specific application.
     * X-User-Role: ADMIN is required by the application-service endpoint.
     */
    @PutMapping("/applications/{id}/status")
    void updateStatus(@PathVariable("id") Long id,
                      @RequestParam("status") String status,
                      @RequestHeader("X-User-Role") String role);

    @GetMapping("/applications/{id}")
    java.util.Map<String, Object> getApplicationById(@PathVariable("id") Long id);
}
