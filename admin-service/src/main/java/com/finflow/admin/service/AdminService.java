package com.finflow.admin.service;

import com.finflow.admin.client.ApplicationServiceClient;
import com.finflow.admin.client.DocumentServiceClient;
import com.finflow.admin.dto.DecisionRequest;
import com.finflow.admin.dto.DecisionResponse;
import com.finflow.admin.entity.Decision;
import com.finflow.admin.repository.DecisionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final DecisionRepository decisionRepository;
    private final ModelMapper modelMapper;
    private final ApplicationServiceClient applicationServiceClient;
    private final DocumentServiceClient documentServiceClient;
    private final com.finflow.admin.client.AuthServiceClient authServiceClient;
    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    // ─────────────────────────────────────────────────────────────
    // GET ALL APPLICATIONS — Fetch all non-draft applications
    // ─────────────────────────────────────────────────────────────
//    public List<?> getAllApplications() {
//        List<?> applications = applicationServiceClient.getAllApplications("ADMIN");
//        log.info("Fetched {} applications",
//                applications != null ? applications.size() : 0);
//        return applications;
//    }
    public Page<?> getAllApplications(int page, int size) {
//        log.info("Fetched {} applications",
//                applications != null ? applications.size() : 0);
        return applicationServiceClient.getAllApplications("ADMIN", page, size);
    }

    // ─────────────────────────────────────────────────────────────
    // MAKE DECISION — Approve or reject an application
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public DecisionResponse makeDecision(Long applicationId,
                                         DecisionRequest request,
                                         Long adminId) {

        // Check if decision already made
        if (decisionRepository.existsByApplicationId(applicationId)) {
            throw new RuntimeException(
                    "Decision already made for this application!");
        }

        // First set status to UNDER_REVIEW
        try {
            applicationServiceClient.updateStatus(applicationId, "UNDER_REVIEW", "ADMIN");
            log.info("Application {} moved to UNDER_REVIEW", applicationId);
        } catch (Exception e) {
            log.warn("Could not move to UNDER_REVIEW (may already be): {}", e.getMessage());
        }

        // Save decision in DB
        Decision decision = new Decision();
        decision.setApplicationId(applicationId);
        decision.setAdminId(adminId);
        decision.setDecision(request.getDecision());
        decision.setRemarks(request.getRemarks());
        decision.setApprovedAmount(request.getApprovedAmount());
        decision.setTenureMonths(request.getTenureMonths());
        decision.setInterestRate(request.getInterestRate());

        Decision saved = decisionRepository.save(decision);
        log.info("Decision {} made for application {}",
                request.getDecision(), applicationId);

        // Update application status via Feign client (APPROVED or REJECTED)
        applicationServiceClient.updateStatus(applicationId, request.getDecision(), "ADMIN");

        // Publish Notification Event
        try {
            Long userId = null;
            String email = "";
            java.util.Map<String, Object> appData = applicationServiceClient.getApplicationById(applicationId);
            if (appData != null && appData.get("userId") != null) {
                userId = Long.valueOf(appData.get("userId").toString());
                java.util.Map<String, Object> user = authServiceClient.getUserById(userId);
                if (user != null && user.get("email") != null) {
                    email = user.get("email").toString();
                }
            }

            if (userId != null) {
                String subject;
                String message;
                String type;
                if ("APPROVED".equals(request.getDecision())) {
                    subject = "Loan Application Approved!";
                    message = "Congratulations! Your loan application #" + applicationId + " has been approved for " + request.getApprovedAmount() + ".";
                    type = "LOAN_APPROVED";
                } else {
                    subject = "Loan Application Rejected";
                    message = "We regret to inform you that your application #" + applicationId + " was rejected. Reason: " + request.getRemarks();
                    type = "LOAN_REJECTED";
                }

                com.finflow.common.event.NotificationEvent event = com.finflow.common.event.NotificationEvent.builder()
                        .userId(userId)
                        .email(email)
                        .subject(subject)
                        .message(message)
                        .type(type)
                        .build();
                rabbitTemplate.convertAndSend(com.finflow.admin.config.RabbitMQConfig.EXCHANGE_NAME, com.finflow.admin.config.RabbitMQConfig.ROUTING_KEY, event);
                log.info("Message published to RabbitMQ successfully for decision");
            }
        } catch (Exception e) {
            log.error("Failed to publish RabbitMQ event for decision: {}", e.getMessage());
        }

        return modelMapper.map(saved, DecisionResponse.class);
    }

    // ─────────────────────────────────────────────────────────────
    // VERIFY DOCUMENT — Delegate to Document Service via Feign
    // Auto-promote application to DOCS_VERIFIED when all docs verified
    // ─────────────────────────────────────────────────────────────
    public Object verifyDocument(Long documentId,
                               boolean approved,
                               Long adminId) {

        Map<String, Object> result = documentServiceClient.verifyDocument(
                documentId, approved, adminId, "ADMIN");

        log.info("Document {} {}", documentId,
                approved ? "verified" : "rejected");

        // Auto-promote to DOCS_VERIFIED if all documents are now verified
        Boolean allDocsVerified = (Boolean) result.get("allDocsVerified");
        if (Boolean.TRUE.equals(allDocsVerified)) {
            Object applicationIdObj = result.get("applicationId");
            if (applicationIdObj != null) {
                Long appId = Long.valueOf(applicationIdObj.toString());
                try {
                    applicationServiceClient.updateStatus(appId, "DOCS_VERIFIED", "ADMIN");
                    log.info("All docs verified — Application {} auto-promoted to DOCS_VERIFIED", appId);
                } catch (Exception e) {
                    log.error("Failed to auto-promote application {} to DOCS_VERIFIED: {}",
                            appId, e.getMessage());
                }
            }
        }

        return result;
    }

    // ─────────────────────────────────────────────────────────────
    // GET REPORTS — Basic statistics
    // ─────────────────────────────────────────────────────────────
    public Map<String, Object> getReports() {
        List<Decision> decisions = decisionRepository.findAll();

        long totalDecisions = decisions.size();
        long approved = decisions.stream()
                .filter(d -> d.getDecision().equals("APPROVED"))
                .count();
        long rejected = decisions.stream()
                .filter(d -> d.getDecision().equals("REJECTED"))
                .count();

        double approvalRate = totalDecisions > 0
                ? (double) approved / totalDecisions * 100 : 0;

        return Map.of(
                "totalDecisions", totalDecisions,
                "approved", approved,
                "rejected", rejected,
                "approvalRate", String.format("%.1f%%", approvalRate)
        );
    }

    public List<Map<String, Object>> getAllUsers() {
        return authServiceClient.getAllUsers();
    }
}