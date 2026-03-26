package com.finflow.admin.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DecisionResponse {
    private Long id;
    private Long applicationId;
    private Long adminId;
    private String decision;
    private String remarks;
    private Double approvedAmount;
    private Integer tenureMonths;
    private Double interestRate;
    private LocalDateTime decidedAt;
}