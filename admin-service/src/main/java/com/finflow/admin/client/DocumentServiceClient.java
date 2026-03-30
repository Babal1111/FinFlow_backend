package com.finflow.admin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

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
    Object verifyDocument(@PathVariable("id") Long documentId,
                          @RequestParam("approved") boolean approved,
                          @RequestHeader("X-User-Id") Long adminId);
}
