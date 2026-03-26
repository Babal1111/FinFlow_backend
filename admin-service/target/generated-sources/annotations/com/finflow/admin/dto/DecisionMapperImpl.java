package com.finflow.admin.dto;

import com.finflow.admin.entity.Decision;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-26T21:10:11+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class DecisionMapperImpl implements DecisionMapper {

    @Override
    public DecisionResponse toResponse(Decision decision) {
        if ( decision == null ) {
            return null;
        }

        DecisionResponse decisionResponse = new DecisionResponse();

        return decisionResponse;
    }
}
