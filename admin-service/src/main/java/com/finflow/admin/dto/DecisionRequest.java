package com.finflow.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DecisionRequest {

    @NotBlank(message = "Decision required hai")
    private String decision;      // APPROVED / REJECTED

    private String remarks;
    private Double approvedAmount;
    private Integer tenureMonths;
    private Double interestRate;
}