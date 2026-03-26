package com.finflow.document.dto;

import com.finflow.document.entity.DocumentStatus;
import com.finflow.document.entity.DocumentType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DocumentResponse {
    private Long id;
    private Long applicationId;
    private Long userId;
    private DocumentType documentType;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private DocumentStatus status;
    private LocalDateTime uploadedAt;
    private LocalDateTime verifiedAt;
}