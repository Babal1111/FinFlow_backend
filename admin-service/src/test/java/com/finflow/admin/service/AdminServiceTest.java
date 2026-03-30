package com.finflow.admin.service;

import com.finflow.admin.client.ApplicationServiceClient;
import com.finflow.admin.client.DocumentServiceClient;
import com.finflow.admin.dto.DecisionRequest;
import com.finflow.admin.dto.DecisionResponse;
import com.finflow.admin.entity.Decision;
import com.finflow.admin.repository.DecisionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private DecisionRepository decisionRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ApplicationServiceClient applicationServiceClient;

    @Mock
    private DocumentServiceClient documentServiceClient;

    @InjectMocks
    private AdminService adminService;

    private Decision savedDecision;
    private DecisionRequest decisionRequest;
    private DecisionResponse decisionResponse;

    @BeforeEach
    void setUp() {
        savedDecision = new Decision();
        savedDecision.setId(1L);
        savedDecision.setApplicationId(100L);
        savedDecision.setAdminId(5L);
        savedDecision.setDecision("APPROVED");
        savedDecision.setRemarks("Looks good");
        savedDecision.setApprovedAmount(45000.0);
        savedDecision.setTenureMonths(24);
        savedDecision.setInterestRate(8.5);

        decisionRequest = new DecisionRequest();
        decisionRequest.setDecision("APPROVED");
        decisionRequest.setRemarks("Looks good");
        decisionRequest.setApprovedAmount(45000.0);
        decisionRequest.setTenureMonths(24);
        decisionRequest.setInterestRate(8.5);

        decisionResponse = new DecisionResponse();
        decisionResponse.setId(1L);
        decisionResponse.setApplicationId(100L);
        decisionResponse.setDecision("APPROVED");
    }

    // ─── getAllApplications ────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllApplications: should return list from application-service via Feign")
    void getAllApplications_shouldReturnList() {
        List<?> mockApps = List.of(Map.of("id", 1), Map.of("id", 2));
        // doReturn() bypasses Java generic type-check for List<?> wildcard
        doReturn(mockApps).when(applicationServiceClient).getAllApplications("ADMIN");

        List<?> results = adminService.getAllApplications();

        assertNotNull(results);
        assertEquals(2, results.size());
        verify(applicationServiceClient).getAllApplications("ADMIN");
    }

    @Test
    @DisplayName("getAllApplications: should handle null response gracefully")
    void getAllApplications_shouldHandleNullBody() {
        doReturn(null).when(applicationServiceClient).getAllApplications("ADMIN");

        List<?> results = adminService.getAllApplications();

        assertNull(results);
    }

    // ─── makeDecision ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("makeDecision: should save decision and call status update via Feign")
    void makeDecision_shouldSaveDecisionAndCallStatusUpdate() {
        when(decisionRepository.existsByApplicationId(100L)).thenReturn(false);
        when(decisionRepository.save(any(Decision.class))).thenReturn(savedDecision);
        when(modelMapper.map(savedDecision, DecisionResponse.class)).thenReturn(decisionResponse);
        doNothing().when(applicationServiceClient).updateStatus(anyLong(), anyString(), eq("ADMIN"));

        DecisionResponse result = adminService.makeDecision(100L, decisionRequest, 5L);

        assertNotNull(result);
        assertEquals("APPROVED", result.getDecision());
        assertEquals(100L, result.getApplicationId());
        verify(decisionRepository).save(any(Decision.class));
        verify(applicationServiceClient).updateStatus(100L, "APPROVED", "ADMIN");
    }

    @Test
    @DisplayName("makeDecision: should throw when decision already exists for application")
    void makeDecision_shouldThrow_whenDecisionAlreadyExists() {
        when(decisionRepository.existsByApplicationId(100L)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> adminService.makeDecision(100L, decisionRequest, 5L));

        assertEquals("Decision already made for this application!", ex.getMessage());
        verify(decisionRepository, never()).save(any());
        verify(applicationServiceClient, never()).updateStatus(any(), any(), any());
    }

    // ─── verifyDocument ───────────────────────────────────────────────────────

    @Test
    @DisplayName("verifyDocument: should delegate to document-service via Feign and return response")
    void verifyDocument_shouldDelegateToDocumentService() {
        Map<String, Object> mockDocResponse = Map.of("id", 1, "status", "VERIFIED");
        when(documentServiceClient.verifyDocument(1L, true, 5L)).thenReturn(mockDocResponse);

        Object result = adminService.verifyDocument(1L, true, 5L);

        assertNotNull(result);
        verify(documentServiceClient).verifyDocument(1L, true, 5L);
    }

    @Test
    @DisplayName("verifyDocument: should pass correct adminId to Feign client")
    void verifyDocument_shouldPassAdminIdToFeignClient() {
        when(documentServiceClient.verifyDocument(2L, false, 7L)).thenReturn(Map.of());

        adminService.verifyDocument(2L, false, 7L);

        verify(documentServiceClient).verifyDocument(2L, false, 7L);
    }

    // ─── getReports ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getReports: should calculate correct statistics from decisions")
    void getReports_shouldCalculateStats() {
        Decision approved1 = new Decision(); approved1.setDecision("APPROVED");
        Decision approved2 = new Decision(); approved2.setDecision("APPROVED");
        Decision rejected1 = new Decision(); rejected1.setDecision("REJECTED");

        when(decisionRepository.findAll()).thenReturn(List.of(approved1, approved2, rejected1));

        Map<String, Object> reports = adminService.getReports();

        assertNotNull(reports);
        assertEquals(3L, reports.get("totalDecisions"));
        assertEquals(2L, reports.get("approved"));
        assertEquals(1L, reports.get("rejected"));
        assertEquals("66.7%", reports.get("approvalRate"));
    }

    @Test
    @DisplayName("getReports: should return zero stats when no decisions exist")
    void getReports_shouldReturnZeroStats_whenNoDecisions() {
        when(decisionRepository.findAll()).thenReturn(List.of());

        Map<String, Object> reports = adminService.getReports();

        assertEquals(0L, reports.get("totalDecisions"));
        assertEquals(0L, reports.get("approved"));
        assertEquals(0L, reports.get("rejected"));
        assertEquals("0.0%", reports.get("approvalRate"));
    }
}
