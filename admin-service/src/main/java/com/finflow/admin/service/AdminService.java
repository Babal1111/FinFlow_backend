package com.finflow.admin.service;

import com.finflow.admin.dto.DecisionMapper;
import com.finflow.admin.dto.DecisionRequest;
import com.finflow.admin.dto.DecisionResponse;
import com.finflow.admin.entity.Decision;
import com.finflow.admin.repository.DecisionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final DecisionRepository decisionRepository;
    private final DecisionMapper mapper;
    private final RestTemplate restTemplate;

    @Value("${application.service.url}")
    private String applicationServiceUrl;

    @Value("${document.service.url}")
    private String documentServiceUrl;

    // ─────────────────────────────────────────────────────────────
    // GET ALL APPLICATIONS — Fetch all non-draft applications
    // ─────────────────────────────────────────────────────────────
    public List<?> getAllApplications() {
        String url = applicationServiceUrl + "/applications/all";
        List<?> applications = restTemplate.getForObject(url, List.class);
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

        // Update application status via REST call
        String statusUrl = applicationServiceUrl +
                "/applications/" + applicationId +
                "/status?status=" + request.getDecision();
        restTemplate.put(statusUrl, null);

        return mapper.toResponse(saved);
    }

    // ─────────────────────────────────────────────────────────────
    // VERIFY DOCUMENT — Delegate to Document Service
    // ─────────────────────────────────────────────────────────────
    public void verifyDocument(Long documentId,
                               boolean approved,
                               Long adminId) {

        String url = documentServiceUrl +
                "/documents/" + documentId +
                "/verify?approved=" + approved;

        // Pass adminId as header to Document Service
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", adminId.toString());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
        log.info("Document {} {}", documentId,
                approved ? "verified" : "rejected");
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