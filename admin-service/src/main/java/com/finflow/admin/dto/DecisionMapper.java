package com.finflow.admin.dto;

import com.finflow.admin.entity.Decision;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DecisionMapper {

    // Entity → Response DTO
    DecisionResponse toResponse(Decision decision);
}