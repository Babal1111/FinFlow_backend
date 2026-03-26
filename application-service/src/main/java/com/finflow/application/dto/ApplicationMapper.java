package com.finflow.application.dto;

import com.finflow.application.entity.LoanApplication;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {


    ApplicationResponse toResponse(LoanApplication application);

    LoanApplication toEntity(ApplicationRequest request);

    void updateEntity(ApplicationRequest request,
                      @MappingTarget LoanApplication application);
}