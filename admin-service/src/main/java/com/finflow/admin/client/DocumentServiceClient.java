package com.finflow.admin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Feign Client for document-service.
 * Service name must match the registered Eureka service name.
 */

@FeignClient(name = "document-service")
public interface DocumentServiceClient {

    /**
     * Approve or reject a document.
     * Admin ID is forwarded as X-User-Id header to document-service.
     */
    @PutMapping("/documents/{id}/verify")
    Map<String, Object> verifyDocument(@PathVariable("id") Long documentId,
                          @RequestParam("approved") boolean approved,
                          @RequestHeader("X-User-Id") Long adminId,
                          @RequestHeader("X-User-Role") String role);

    /**
     * Get all documents for a specific application.
     */
    @GetMapping("/documents/application/{applicationId}")
    List<?> getDocsByApplication(@PathVariable("applicationId") Long applicationId);

}
