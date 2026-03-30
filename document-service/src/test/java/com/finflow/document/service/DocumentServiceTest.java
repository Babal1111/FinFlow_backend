package com.finflow.document.service;

import com.finflow.document.dto.DocumentResponse;
import com.finflow.document.entity.Document;
import com.finflow.document.entity.DocumentStatus;
import com.finflow.document.entity.DocumentType;
import com.finflow.document.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private DocumentService documentService;

    @TempDir
    Path tempDir; // JUnit creates a real temp directory for file I/O

    private Document savedDocument;
    private DocumentResponse documentResponse;

    @BeforeEach
    void setUp() {
        // Inject the temp directory path into the service's @Value field
        ReflectionTestUtils.setField(documentService, "uploadDir", tempDir.toString());

        savedDocument = new Document();
        savedDocument.setId(1L);
        savedDocument.setApplicationId(100L);
        savedDocument.setUserId(10L);
        savedDocument.setDocumentType(DocumentType.AADHAAR);
        savedDocument.setFileName("test.pdf");
        savedDocument.setFileType("application/pdf");
        savedDocument.setFileSize(1024L);
        savedDocument.setStatus(DocumentStatus.PENDING);

        documentResponse = new DocumentResponse();
        documentResponse.setId(1L);
        documentResponse.setApplicationId(100L);
        documentResponse.setStatus(DocumentStatus.PENDING);
    }

    // ─── upload ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("upload: should save document and return response for valid PDF")
    void upload_shouldSaveAndReturnResponse() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf",
                "PDF content".getBytes());

        when(documentRepository.save(any(Document.class))).thenReturn(savedDocument);
        when(modelMapper.map(savedDocument, DocumentResponse.class)).thenReturn(documentResponse);

        DocumentResponse result = documentService.upload(file, 100L, "AADHAAR", 10L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(100L, result.getApplicationId());
        verify(documentRepository).save(any(Document.class));
    }

    @Test
    @DisplayName("upload: should throw when file type is not PDF/JPG/PNG")
    void upload_shouldThrow_whenInvalidFileType() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "malware.exe", "application/octet-stream",
                "some content".getBytes());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> documentService.upload(file, 100L, "AADHAR", 10L));

        assertEquals("Only PDF, JPG, PNG files allowed!", ex.getMessage());
        verify(documentRepository, never()).save(any());
    }

    @Test
    @DisplayName("upload: should throw when file size exceeds 5MB")
    void upload_shouldThrow_whenFileTooLarge() {
        // Create a mock file that reports size > 5MB
        byte[] content = new byte[6 * 1024 * 1024]; // 6 MB
        MockMultipartFile file = new MockMultipartFile(
                "file", "big.pdf", "application/pdf", content);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> documentService.upload(file, 100L, "AADHAR", 10L));

        assertEquals("File size must be less than 5MB!", ex.getMessage());
        verify(documentRepository, never()).save(any());
    }

    @Test
    @DisplayName("upload: should accept JPEG files")
    void upload_shouldAcceptJpeg() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg",
                "JPEG content".getBytes());

        when(documentRepository.save(any(Document.class))).thenReturn(savedDocument);
        when(modelMapper.map(savedDocument, DocumentResponse.class)).thenReturn(documentResponse);

        DocumentResponse result = documentService.upload(file, 100L, "PHOTOGRAPH", 10L);

        assertNotNull(result);
        verify(documentRepository).save(any(Document.class));
    }

    // ─── getByApplicationId ───────────────────────────────────────────────────

    @Test
    @DisplayName("getByApplicationId: should return list of document responses")
    void getByApplicationId_shouldReturnList() {
        Document doc2 = new Document();
        doc2.setId(2L);

        DocumentResponse response2 = new DocumentResponse();
        response2.setId(2L);

        when(documentRepository.findByApplicationId(100L)).thenReturn(List.of(savedDocument, doc2));
        when(modelMapper.map(savedDocument, DocumentResponse.class)).thenReturn(documentResponse);
        when(modelMapper.map(doc2, DocumentResponse.class)).thenReturn(response2);

        List<DocumentResponse> results = documentService.getByApplicationId(100L);

        assertEquals(2, results.size());
        verify(documentRepository).findByApplicationId(100L);
    }

    // ─── verify ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("verify: should set status to VERIFIED when approved=true")
    void verify_shouldApproveDocument() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(savedDocument));
        when(documentRepository.save(savedDocument)).thenReturn(savedDocument);

        DocumentResponse verifiedResponse = new DocumentResponse();
        verifiedResponse.setId(1L);
        verifiedResponse.setStatus(DocumentStatus.VERIFIED);
        when(modelMapper.map(savedDocument, DocumentResponse.class)).thenReturn(verifiedResponse);

        DocumentResponse result = documentService.verify(1L, 99L, true);

        assertNotNull(result);
        assertEquals(DocumentStatus.VERIFIED, savedDocument.getStatus());
        assertEquals(99L, savedDocument.getVerifiedBy());
        assertNotNull(savedDocument.getVerifiedAt());
        verify(documentRepository).save(savedDocument);
    }

    @Test
    @DisplayName("verify: should set status to REJECTED when approved=false")
    void verify_shouldRejectDocument() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(savedDocument));
        when(documentRepository.save(savedDocument)).thenReturn(savedDocument);

        DocumentResponse rejectedResponse = new DocumentResponse();
        rejectedResponse.setStatus(DocumentStatus.REJECTED);
        when(modelMapper.map(savedDocument, DocumentResponse.class)).thenReturn(rejectedResponse);

        DocumentResponse result = documentService.verify(1L, 99L, false);

        assertEquals(DocumentStatus.REJECTED, savedDocument.getStatus());
    }

    @Test
    @DisplayName("verify: should throw when document not found")
    void verify_shouldThrow_whenDocumentNotFound() {
        when(documentRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> documentService.verify(99L, 1L, true));

        assertEquals("Document not found!", ex.getMessage());
        verify(documentRepository, never()).save(any());
    }
}
