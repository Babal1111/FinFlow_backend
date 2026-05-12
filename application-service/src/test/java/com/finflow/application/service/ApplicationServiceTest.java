package com.finflow.application.service;

import com.finflow.application.dto.ApplicationRequest;
import com.finflow.application.dto.ApplicationResponse;
import com.finflow.application.entity.ApplicationStatus;
import com.finflow.application.entity.LoanApplication;
import com.finflow.application.repository.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ApplicationService applicationService;

    private LoanApplication sampleApp;
    private ApplicationRequest sampleRequest;

    @BeforeEach
    void setup() {
        // Test ke liye basic data ready karna
        sampleApp = new LoanApplication();
        sampleApp.setId(1L);
        sampleApp.setUserId(100L);
        sampleApp.setStatus(ApplicationStatus.DRAFT);
        sampleApp.setFirstName("John");
        sampleApp.setLoanAmount(5000.0);
        sampleApp.setMonthlyIncome(2000.0);

        sampleRequest = new ApplicationRequest();
        sampleRequest.setLoanAmount(5000.0);
        sampleRequest.setFirstName("John");
    }

    @Test
    void createApplication_Success() {
        // 1. Mocking: Jab mapper aur repo call ho toh kya return karna hai
        when(modelMapper.map(any(), any())).thenReturn(sampleApp);
        when(applicationRepository.save(any())).thenReturn(sampleApp);

        // 2. Execution: Service ka method call karna
        ApplicationResponse result = applicationService.create(sampleRequest, 100L);

        // 3. Verification: Result sahi hai ya nahi
        assertNotNull(result);
        verify(applicationRepository, times(1)).save(any());
    }

    @Test
    void submitApplication_Success() {
        // Setup: Application ID 1 milni chahiye
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(sampleApp));
        when(applicationRepository.save(any())).thenReturn(sampleApp);

        // Call method
        applicationService.submit(1L, 100L);

        // Check: Status SUBMITTED hona chahiye
        assertEquals(ApplicationStatus.SUBMITTED, sampleApp.getStatus());
    }

    @Test
    void submitApplication_ThrowsError_WhenNotDraft() {
        // Status change karke dekhte hain ki error aata hai ya nahi
        sampleApp.setStatus(ApplicationStatus.SUBMITTED);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(sampleApp));

        // Expectation: Error aana chahiye
        assertThrows(RuntimeException.class, () -> {
            applicationService.submit(1L, 100L);
        });
    }

    @Test
    void updateStatus_ByAdmin_Success() {
        // Repo mock
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(sampleApp));

        // Status change logic: DRAFT -> SUBMITTED (according to VALID_TRANSITIONS in service)
        applicationService.updateStatus(1L, ApplicationStatus.SUBMITTED);

        assertEquals(ApplicationStatus.SUBMITTED, sampleApp.getStatus());
        verify(applicationRepository).save(sampleApp);
    }

    @Test
    void getById_ThrowsError_WhenNotFound() {
        // Jab ID na mile
        when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            applicationService.getById(99L);
        });
    }
}
