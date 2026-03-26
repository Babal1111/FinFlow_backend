package com.finflow.document.dto;

import com.finflow.document.entity.Document;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    // Entity → Response DTO
    DocumentResponse toResponse(Document document);
}