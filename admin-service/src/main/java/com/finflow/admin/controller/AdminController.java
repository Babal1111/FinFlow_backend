package com.finflow.admin.controller;

import com.finflow.admin.dto.DecisionRequest;
import com.finflow.admin.dto.DecisionResponse;
import com.finflow.admin.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;


    // GET /admin/applications
    // Fetch all non-draft applications for admin review

    @GetMapping("/applications")
    public ResponseEntity<List<?>> getAllApplications(
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("ADMIN")) {
            throw new RuntimeException("Access denied!");
        }

        log.info("Fetch all applications request");
        return ResponseEntity.ok(adminService.getAllApplications());
    }

    // ─────────────────────────────────────────────────────────────
    // POST /admin/applications/{id}/decision
    // Approve or reject an application
    // ─────────────────────────────────────────────────────────────
    @PostMapping("/applications/{id}/decision")
    public ResponseEntity<DecisionResponse> makeDecision(
            @PathVariable Long id,
            @Valid @RequestBody DecisionRequest request,
            @RequestHeader("X-User-Id") Long adminId,
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("ADMIN")) {
            throw new RuntimeException("Access denied!");
        }

        log.info("Decision request for application: {}", id);
        return ResponseEntity.ok(
                adminService.makeDecision(id, request, adminId));
    }

    // ─────────────────────────────────────────────────────────────
    // PUT /admin/documents/{id}/verify
    // Verify or reject a document
    // ─────────────────────────────────────────────────────────────
    @PutMapping("/documents/{id}/verify")
    public ResponseEntity<Object> verifyDocument(
            @PathVariable Long id,
            @RequestParam boolean approved,
            @RequestHeader("X-User-Id") Long adminId,
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("ADMIN")) {
            throw new RuntimeException("Access denied!");
        }

        log.info("Verify document request for id: {}", id);
        Object documentResponse = adminService.verifyDocument(id, approved, adminId);
        return ResponseEntity.ok(documentResponse);
    }

    // ─────────────────────────────────────────────────────────────
    // GET /admin/reports
    // Get approval statistics and reports
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/reports")
    public ResponseEntity<Map<String, Object>> getReports(
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("ADMIN")) {
            throw new RuntimeException("Access denied!");
        }

        log.info("Reports request");
        return ResponseEntity.ok(adminService.getReports());
    }
}