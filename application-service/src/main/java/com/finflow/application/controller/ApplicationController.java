package com.finflow.application.controller;

import com.finflow.application.dto.ApplicationRequest;
import com.finflow.application.dto.ApplicationResponse;
import com.finflow.application.dto.StatusResponse;
import com.finflow.application.entity.ApplicationStatus;
import com.finflow.application.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    public ResponseEntity<ApplicationResponse> create(
            @Valid @RequestBody ApplicationRequest request,
            @RequestHeader("X-User-Id") Long userId) {


        log.info("Create application request for userId: {}", userId);
        ApplicationResponse response = applicationService.create(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PutMapping("/{id}")
    public ResponseEntity<ApplicationResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationRequest request,
            @RequestHeader("X-User-Id") Long userId) {

        log.info("Update application request for id: {}", id);
        ApplicationResponse response = applicationService.update(id, request, userId);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/{id}/submit")
    public ResponseEntity<Void> submit(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {

        log.info("Submit application request for id: {}", id);
        applicationService.submit(id, userId);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/my")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications(
            @RequestHeader("X-User-Id") Long userId) {

        log.info("Get applications for userId: {}", userId);
        List<ApplicationResponse> responses =
                applicationService.getMyApplications(userId);
        return ResponseEntity.ok(responses);
    }


    @GetMapping("/{id}/status")
    public ResponseEntity<StatusResponse> getStatus(
            @PathVariable("id") Long id,
            @RequestHeader("X-User-Id") Long userId) {

        log.info("Get status for application id: {}", id);
        StatusResponse response = applicationService.getStatus(id, userId);
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") ApplicationStatus status) {

        log.info("Update status for application id: {} to {}", id, status);
        applicationService.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<ApplicationResponse>> getAllApplications(
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("ADMIN")) {
            throw new RuntimeException("Access denied!");
        }

        log.info("Fetch all applications for admin");
        return ResponseEntity.ok(applicationService.getAllApplications());
    }
}
