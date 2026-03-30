package com.finflow.application.service;

import com.finflow.application.dto.ApplicationRequest;
import com.finflow.application.dto.ApplicationResponse;
import com.finflow.application.dto.StatusResponse;
import com.finflow.application.entity.ApplicationStatus;
import com.finflow.application.entity.LoanApplication;
import com.finflow.application.repository.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ApplicationService applicationService;

    private LoanApplication draftApplication;
    private ApplicationRequest request;
    private ApplicationResponse response;

    @BeforeEach
    void setUp() {
        draftApplication = new LoanApplication();
        draftApplication.setId(1L);
        draftApplication.setUserId(10L);
        draftApplication.setStatus(ApplicationStatus.DRAFT);
        draftApplication.setFirstName("John");
        draftApplication.setLoanAmount(50000.0);
        draftApplication.setMonthlyIncome(8000.0);

        request = new ApplicationRequest();
        request.setLoanAmount(50000.0);
        request.setTenureMonths(24);
        request.setFirstName("John");
        request.setMonthlyIncome(8000.0);

        response = new ApplicationResponse();
        response.setId(1L);
        response.setUserId(10L);
        response.setStatus(ApplicationStatus.DRAFT);
        response.setLoanAmount(50000.0);
    }

    // ─── create ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create: should save and return ApplicationResponse")
    void create_shouldReturnResponse() {
        when(modelMapper.map(request, LoanApplication.class)).thenReturn(draftApplication);
        when(applicationRepository.save(draftApplication)).thenReturn(draftApplication);
        when(modelMapper.map(draftApplication, ApplicationResponse.class)).thenReturn(response);

        ApplicationResponse result = applicationService.create(request, 10L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(10L, result.getUserId());
        verify(applicationRepository).save(draftApplication);
    }

    // ─── update ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update: should update a DRAFT application successfully")
    void update_shouldUpdateDraftApplication() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(draftApplication));
        when(applicationRepository.save(draftApplication)).thenReturn(draftApplication);
        // modelMapper.map(request, application) is a void 2-arg form — no stub needed, just verify
        // modelMapper.map(saved, ApplicationResponse.class) is the return form — stub with any()
        lenient().when(modelMapper.map(any(), eq(ApplicationResponse.class))).thenReturn(response);

        ApplicationResponse result = applicationService.update(1L, request, 10L);

        assertNotNull(result);
        verify(modelMapper).map(eq(request), eq(draftApplication)); // void form called
        verify(applicationRepository).save(draftApplication);
    }

    @Test
    @DisplayName("update: should throw when application status is not DRAFT")
    void update_shouldThrow_whenNotDraft() {
        draftApplication.setStatus(ApplicationStatus.SUBMITTED);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(draftApplication));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> applicationService.update(1L, request, 10L));

        assertEquals("Only DRAFT application can be updated!", ex.getMessage());
        verify(applicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("update: should throw when userId does not match")
    void update_shouldThrow_whenUserMismatch() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(draftApplication));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> applicationService.update(1L, request, 99L)); // wrong userId

        assertEquals("Access denied!", ex.getMessage());
    }

    // ─── submit ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("submit: should set status to SUBMITTED and return ApplicationResponse")
    void submit_shouldSetStatusSubmitted() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(draftApplication));
        when(applicationRepository.save(any())).thenReturn(draftApplication);
        when(modelMapper.map(draftApplication, ApplicationResponse.class)).thenReturn(response);

        ApplicationResponse result = applicationService.submit(1L, 10L);

        assertNotNull(result);
        assertEquals(ApplicationStatus.SUBMITTED, draftApplication.getStatus());
        assertNotNull(draftApplication.getSubmittedAt());
        verify(applicationRepository).save(draftApplication);
    }

    @Test
    @DisplayName("submit: should throw when application is not DRAFT")
    void submit_shouldThrow_whenNotDraft() {
        draftApplication.setStatus(ApplicationStatus.SUBMITTED);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(draftApplication));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> applicationService.submit(1L, 10L));

        assertEquals("Only DRAFT application can be submitted!", ex.getMessage());
    }

    @Test
    @DisplayName("submit: should throw when required fields are missing")
    void submit_shouldThrow_whenRequiredFieldsMissing() {
        draftApplication.setFirstName(null); // required field missing
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(draftApplication));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> applicationService.submit(1L, 10L));

        assertEquals("Please fill all required details before submitting!", ex.getMessage());
        verify(applicationRepository, never()).save(any());
    }

    // ─── getMyApplications ────────────────────────────────────────────────────

    @Test
    @DisplayName("getMyApplications: should return list of responses for userId")
    void getMyApplications_shouldReturnList() {
        LoanApplication app2 = new LoanApplication();
        app2.setId(2L);
        app2.setUserId(10L);
        app2.setStatus(ApplicationStatus.SUBMITTED);

        ApplicationResponse resp2 = new ApplicationResponse();
        resp2.setId(2L);

        when(applicationRepository.findByUserId(10L)).thenReturn(List.of(draftApplication, app2));
        when(modelMapper.map(draftApplication, ApplicationResponse.class)).thenReturn(response);
        when(modelMapper.map(app2, ApplicationResponse.class)).thenReturn(resp2);

        List<ApplicationResponse> results = applicationService.getMyApplications(10L);

        assertEquals(2, results.size());
        verify(applicationRepository).findByUserId(10L);
    }

    // ─── getStatus ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getStatus: should return StatusResponse for valid application")
    void getStatus_shouldReturnStatusResponse() {
        draftApplication.setUpdatedAt(LocalDateTime.now());
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(draftApplication));

        StatusResponse result = applicationService.getStatus(1L, 10L);

        assertNotNull(result);
        assertEquals(1L, result.getApplicationId());
        assertEquals(ApplicationStatus.DRAFT, result.getCurrentStatus());
    }

    // ─── updateStatus ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateStatus: should update status and save")
    void updateStatus_shouldUpdateAndSave() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(draftApplication));
        when(applicationRepository.save(any())).thenReturn(draftApplication);

        applicationService.updateStatus(1L, ApplicationStatus.APPROVED);

        assertEquals(ApplicationStatus.APPROVED, draftApplication.getStatus());
        verify(applicationRepository).save(draftApplication);
    }

    @Test
    @DisplayName("updateStatus: should throw when application not found")
    void updateStatus_shouldThrow_whenNotFound() {
        when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> applicationService.updateStatus(99L, ApplicationStatus.APPROVED));

        assertEquals("Application not found!", ex.getMessage());
    }

    // ─── getAllApplications ────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllApplications: should exclude DRAFT applications")
    void getAllApplications_shouldExcludeDrafts() {
        LoanApplication submitted = new LoanApplication();
        submitted.setId(2L);
        submitted.setStatus(ApplicationStatus.SUBMITTED);

        ApplicationResponse submittedResp = new ApplicationResponse();
        submittedResp.setId(2L);
        submittedResp.setStatus(ApplicationStatus.SUBMITTED);

        // draftApplication has status=DRAFT → should be excluded
        when(applicationRepository.findAll()).thenReturn(List.of(draftApplication, submitted));
        when(modelMapper.map(submitted, ApplicationResponse.class)).thenReturn(submittedResp);

        List<ApplicationResponse> results = applicationService.getAllApplications();

        assertEquals(1, results.size());
        assertEquals(ApplicationStatus.SUBMITTED, results.get(0).getStatus());
    }
}
