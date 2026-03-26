package com.finflow.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplicationRequest {

    @NotNull(message = "Loan amount required hai")
    @Min(value = 1000, message = "Minimum loan amount 1000 hona chahiye")
    private Double loanAmount;

    private String purpose;

    @NotNull(message = "Tenure required hai")
    private Integer tenureMonths;

    // Personal details
    private String firstName;
    private String lastName;
    private String phone;
    private String address;

    // Employment details
    private String employerName;
    private String employmentType;

    @Min(value = 0, message = "Monthly income valid honi chahiye")
    private Double monthlyIncome;
}