package com.finflow.document.controller;

import com.finflow.document.dto.DocumentResponse;
import com.finflow.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            @PathVariable Long applicationId) {

        List<DocumentResponse> responses =
                documentService.getByApplicationId(applicationId);
        return ResponseEntity.ok(responses);
    }

    // PUT /documents/{id}/verify
    @PutMapping("/{id}/verify")
    public ResponseEntity<DocumentResponse> verify(
            @PathVariable Long id,
            @RequestParam boolean approved,
            @RequestHeader("X-User-Id") Long adminId) {

        log.info("Verify request for document id: {}", id);
        DocumentResponse response = documentService.verify(id, adminId, approved);
        return ResponseEntity.ok(response);
    }
}