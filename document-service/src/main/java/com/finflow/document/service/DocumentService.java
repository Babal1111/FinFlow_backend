package com.finflow.document.service;

import com.finflow.document.dto.DocumentMapper;
import com.finflow.document.dto.DocumentResponse;
import com.finflow.document.entity.Document;
import com.finflow.document.entity.DocumentStatus;
import com.finflow.document.entity.DocumentType;
import com.finflow.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    private final DocumentMapper mapper;

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

        return mapper.toResponse(saved);
    }


    public List<DocumentResponse> getByApplicationId(Long applicationId) {
        return documentRepository.findByApplicationId(applicationId)
                .stream()
                .map(mapper::toResponse)
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

        return mapper.toResponse(updated);
    }
}