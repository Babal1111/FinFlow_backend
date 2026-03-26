package com.finflow.application.dto;

import com.finflow.application.entity.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class StatusResponse {

    private Long applicationId;
    private ApplicationStatus currentStatus;
    private LocalDateTime lastUpdated;

}