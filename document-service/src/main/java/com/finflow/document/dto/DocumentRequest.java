package com.finflow.document.dto;

import lombok.Data;

@Data
public class DocumentRequest {
    private Long applicationId;
    private String documentType;
}