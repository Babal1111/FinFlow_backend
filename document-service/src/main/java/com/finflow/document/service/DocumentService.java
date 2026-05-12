package com.finflow.document.service;

import org.modelmapper.ModelMapper;
import com.finflow.document.dto.DocumentResponse;
import com.finflow.document.entity.Document;
import com.finflow.document.entity.DocumentStatus;
import com.finflow.document.entity.DocumentType;
import com.finflow.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final ModelMapper modelMapper;
    private final com.finflow.document.client.AuthServiceClient authServiceClient;
    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @Value("${app.upload.dir}")
    private String uploadDir;


    public DocumentResponse upload(MultipartFile file,
                                   Long applicationId,
                                   String documentType,
                                   Long userId) throws IOException {

        // Validate file type — only PDF, JPG, PNG allowed
        String fileType = file.getContentType();
        if (fileType == null || (!fileType.equals("application/pdf") &&
                !fileType.equals("image/jpeg") &&
                !fileType.equals("image/png"))) {
            throw new RuntimeException("Only PDF, JPG, PNG files allowed!");
        }

        // Validate file size — max 5MB
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("File size must be less than 5MB!");
        }

        // Generate unique filename to avoid conflicts
        String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        // Create upload directory if it does not exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Save file to disk
        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath);

        // Save document record in DB
        Document document = new Document();
        document.setApplicationId(applicationId);
        document.setUserId(userId);
        document.setDocumentType(DocumentType.valueOf(documentType));
        document.setFileName(file.getOriginalFilename());
        document.setFilePath(filePath.toString());
        document.setFileType(fileType);
        document.setFileSize(file.getSize());
        // Status will be set to PENDING automatically via @PrePersist

        Document saved = documentRepository.save(document);
        log.info("Document uploaded with id: {}", saved.getId());

        return modelMapper.map(saved, DocumentResponse.class);
    }


    public List<DocumentResponse> getByApplicationId(Long applicationId) {
        return documentRepository.findByApplicationId(applicationId)
                .stream()
                .map(doc -> modelMapper.map(doc, DocumentResponse.class))
                .collect(Collectors.toList());
    }


    public DocumentResponse verify(Long documentId,
                                   Long adminId,
                                   boolean approved) {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() ->
                        new RuntimeException("Document not found!"));

        // Set status based on admin decision
        document.setStatus(approved ?
                DocumentStatus.VERIFIED : DocumentStatus.REJECTED);
        document.setVerifiedBy(adminId);
        document.setVerifiedAt(LocalDateTime.now());

        Document updated = documentRepository.save(document);
        log.info("Document {} {}", documentId,
                approved ? "verified" : "rejected");

        DocumentResponse response = modelMapper.map(updated, DocumentResponse.class);

        // Fetch user email
        String email = "";
        try {
            java.util.Map<String, Object> user = authServiceClient.getUserById(document.getUserId());
            if (user != null && user.get("email") != null) {
                email = user.get("email").toString();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch user email for user {}: {}", document.getUserId(), e.getMessage());
        }

        // Notify if rejected
        if (!approved) {
            try {
                com.finflow.common.event.NotificationEvent event = com.finflow.common.event.NotificationEvent.builder()
                        .userId(document.getUserId())
                        .email(email)
                        .subject("Document Rejected - Re-upload Required")
                        .message("Your document " + document.getDocumentType() + " for application #" + document.getApplicationId() + " was rejected. Please re-upload.")
                        .type("DOCUMENT_REJECTED")
                        .build();
                rabbitTemplate.convertAndSend(com.finflow.document.config.RabbitMQConfig.EXCHANGE_NAME, com.finflow.document.config.RabbitMQConfig.ROUTING_KEY, event);
                log.info("Message published to RabbitMQ successfully for document rejection");
            } catch (Exception e) {
                log.error("Failed to publish RabbitMQ event: {}", e.getMessage());
            }
        }

        // Check if ALL documents for this application are now VERIFIED
        Long applicationId = document.getApplicationId();
        List<Document> allDocs = documentRepository.findByApplicationId(applicationId);
        boolean allVerified = !allDocs.isEmpty() && allDocs.stream()
                .allMatch(d -> d.getStatus() == DocumentStatus.VERIFIED);
        response.setAllDocsVerified(allVerified);

        if (allVerified) {
            try {
                com.finflow.common.event.NotificationEvent event = com.finflow.common.event.NotificationEvent.builder()
                        .userId(document.getUserId())
                        .email(email)
                        .subject("All Documents Verified")
                        .message("All your documents for application #" + applicationId + " have been successfully verified. Your application is now under review.")
                        .type("DOCS_VERIFIED")
                        .build();
                rabbitTemplate.convertAndSend(com.finflow.document.config.RabbitMQConfig.EXCHANGE_NAME, com.finflow.document.config.RabbitMQConfig.ROUTING_KEY, event);
                log.info("Message published to RabbitMQ successfully for docs verified");
            } catch (Exception e) {
                log.error("Failed to publish RabbitMQ event: {}", e.getMessage());
            }
        }

        log.info("Application {} — all docs verified: {}", applicationId, allVerified);

        return response;
    }

    public Resource getDocumentResource(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found!"));

        Path path = Paths.get(document.getFilePath());
        if (!Files.exists(path)) {
            throw new RuntimeException("Document file is missing on server!");
        }

        return new FileSystemResource(path);
    }

    public DocumentResponse getById(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found!"));
        return modelMapper.map(document, DocumentResponse.class);
    }
}