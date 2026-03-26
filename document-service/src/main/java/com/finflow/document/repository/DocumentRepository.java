package com.finflow.document.repository;

import com.finflow.document.entity.Document;
import com.finflow.document.entity.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByApplicationId(Long applicationId);
    List<Document> findByApplicationIdAndStatus(Long applicationId,
                                                DocumentStatus status);
    long countByApplicationIdAndStatus(Long applicationId,
                                       DocumentStatus status);
}