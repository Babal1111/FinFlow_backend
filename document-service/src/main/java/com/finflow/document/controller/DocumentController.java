package com.finflow.document.controller;

import com.finflow.document.dto.DocumentResponse;
import com.finflow.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    // POST /documents/upload
    @PostMapping("/upload")
    public ResponseEntity<DocumentResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("applicationId") Long applicationId,
            @RequestParam("documentType") String documentType,
            @RequestHeader("X-User-Id") Long userId) throws IOException {

        log.info("Upload request for applicationId: {}", applicationId);
        DocumentResponse response = documentService.upload(
                file, applicationId, documentType, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /documents/application/{applicationId}
    @GetMapping("/application/{applicationId}")
    public ResponseEntity<List<DocumentResponse>> getByApplication(
            @PathVariable("applicationId") Long applicationId) {


        List<DocumentResponse> responses =
                documentService.getByApplicationId(applicationId);
        return ResponseEntity.ok(responses);
    }

    // PUT /documents/{id}/verify
    @PutMapping("/{id}/verify")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DocumentResponse> verify(
            @PathVariable("id") Long id,
            @RequestParam("approved") boolean approved,
            @RequestHeader("X-User-Id") Long adminId,

            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("ADMIN")) {
            throw new RuntimeException("Access denied!");
        }
        log.info("Verify request for document id: {}", id);
        DocumentResponse response = documentService.verify(id, adminId, approved);
        return ResponseEntity.ok(response);
    }

    // GET /documents/{id}/view
    @GetMapping("/{id}/view")
    public ResponseEntity<Resource> viewDocument(
            @PathVariable("id") Long id,
            @RequestHeader("X-User-Role") String role) {

        // Restrict raw file view to admins.
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("Access denied!");

//            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        DocumentResponse doc = documentService.getById(id);
        Resource resource = documentService.getDocumentResource(id);

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (doc.getFileType() != null && !doc.getFileType().isBlank()) {
            mediaType = MediaType.parseMediaType(doc.getFileType());
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getFileName() + "\"")
                .body(resource);
    }
}