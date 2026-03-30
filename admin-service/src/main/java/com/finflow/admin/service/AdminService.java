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

    // ─────────────────────────────────────────────────────────────
    // GET ALL APPLICATIONS — Fetch all non-draft applications
    // ─────────────────────────────────────────────────────────────
    public List<?> getAllApplications() {
        List<?> applications = applicationServiceClient.getAllApplications("ADMIN");
        log.info("Fetched {} applications",
                applications != null ? applications.size() : 0);
        return applications;
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

        // Update application status via Feign client
        applicationServiceClient.updateStatus(applicationId, request.getDecision(), "ADMIN");

        return modelMapper.map(saved, DecisionResponse.class);
    }

    // ─────────────────────────────────────────────────────────────
    // VERIFY DOCUMENT — Delegate to Document Service via Feign
    // ─────────────────────────────────────────────────────────────
    public Object verifyDocument(Long documentId,
                               boolean approved,
                               Long adminId) {

        Object result = documentServiceClient.verifyDocument(documentId, approved, adminId);
        log.info("Document {} {}", documentId,
                approved ? "verified" : "rejected");
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
}