package com.finflow.document.dto;

import com.finflow.document.entity.Document;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-26T14:45:06+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class DocumentMapperImpl implements DocumentMapper {

    @Override
    public DocumentResponse toResponse(Document document) {
        if ( document == null ) {
            return null;
        }

        DocumentResponse documentResponse = new DocumentResponse();

        documentResponse.setId( document.getId() );
        documentResponse.setApplicationId( document.getApplicationId() );
        documentResponse.setUserId( document.getUserId() );
        documentResponse.setDocumentType( document.getDocumentType() );
        documentResponse.setFileName( document.getFileName() );
        documentResponse.setFileType( document.getFileType() );
        documentResponse.setFileSize( document.getFileSize() );
        documentResponse.setStatus( document.getStatus() );
        documentResponse.setUploadedAt( document.getUploadedAt() );
        documentResponse.setVerifiedAt( document.getVerifiedAt() );

        return documentResponse;
    }
}
