package com.finflow.application.dto;

import com.finflow.application.entity.ApplicationStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApplicationResponse {

    private Long id;
    private Long userId;
    private ApplicationStatus status;

    // Loan details
    private Double loanAmount;
    private String purpose;
    private Integer tenureMonths;

    // Personal details
    private String firstName;
    private String lastName;
    private String phone;
    private String address;

    // Employment details
    private String employerName;
    private String employmentType;
    private Double monthlyIncome;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime submittedAt;
}