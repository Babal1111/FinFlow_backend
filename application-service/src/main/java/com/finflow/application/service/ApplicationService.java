package com.finflow.application.service;

import org.modelmapper.ModelMapper;
import com.finflow.application.dto.ApplicationRequest;
import com.finflow.application.dto.ApplicationResponse;
import com.finflow.application.dto.StatusResponse;
import com.finflow.application.entity.ApplicationStatus;
import com.finflow.application.entity.LoanApplication;
import com.finflow.application.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ModelMapper modelMapper;

    public ApplicationResponse create(ApplicationRequest request, Long userId) {


        LoanApplication application = modelMapper.map(request, LoanApplication.class);


        application.setUserId(userId);


        LoanApplication saved = applicationRepository.save(application);
        log.info("Application created with id: {}", saved.getId());

        return modelMapper.map(saved, ApplicationResponse.class);
    }


    public ApplicationResponse update(Long id, ApplicationRequest request,
                                      Long userId) {


        LoanApplication application = getApplicationByIdAndUserId(id, userId);


        if (application.getStatus() != ApplicationStatus.DRAFT) {
            throw new RuntimeException(
                    "Only DRAFT application can be updated!");
        }


        modelMapper.map(request, application);

        LoanApplication updated = applicationRepository.save(application);
        log.info("Application updated with id: {}", updated.getId());

        return modelMapper.map(updated, ApplicationResponse.class);
    }


    @Transactional
    public void submit(Long id, Long userId) {
        LoanApplication application = getApplicationByIdAndUserId(id, userId);
        if (application.getStatus() != ApplicationStatus.DRAFT) {
            throw new RuntimeException(
                    "Only DRAFT application can be submitted!");
        }
        if (application.getFirstName() == null ||
                application.getLoanAmount() == null ||
                application.getMonthlyIncome() == null) {
            throw new RuntimeException(
                    "Please fill all required details before submitting!");
        }


        application.setStatus(ApplicationStatus.SUBMITTED);
        application.setSubmittedAt(LocalDateTime.now());

        applicationRepository.save(application);
        log.info("Application submitted with id: {}", id);
    }


    public List<ApplicationResponse> getMyApplications(Long userId) {
        return applicationRepository.findByUserId(userId)
                .stream()
                .map(app -> modelMapper.map(app, ApplicationResponse.class))
                .collect(Collectors.toList());
    }


    public StatusResponse getStatus(Long id, Long userId) {
        LoanApplication application = getApplicationByIdAndUserId(id, userId);
        return new StatusResponse(
                application.getId(),
                application.getStatus(),
                application.getUpdatedAt()
        );
    }


    @Transactional
    public void updateStatus(Long id, ApplicationStatus newStatus) {
        LoanApplication application = applicationRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Application not found!"));

        application.setStatus(newStatus);
        applicationRepository.save(application);
        log.info("Application {} status updated to {}", id, newStatus);
    }


    private LoanApplication getApplicationByIdAndUserId(Long id, Long userId) {
        LoanApplication application = applicationRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Application not found!"));

        if (!application.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied!");
        }

        return application;
    }
    public List<ApplicationResponse> getAllApplications() {
        return applicationRepository.findAll()
                .stream()
                .filter(app -> app.getStatus() != ApplicationStatus.DRAFT) // exclude drafts
                .map(app -> modelMapper.map(app, ApplicationResponse.class))
                .collect(Collectors.toList());
    }
}