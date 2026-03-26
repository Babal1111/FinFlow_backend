package com.finflow.application.dto;

import com.finflow.application.entity.LoanApplication;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-26T21:10:50+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class ApplicationMapperImpl implements ApplicationMapper {

    @Override
    public ApplicationResponse toResponse(LoanApplication application) {
        if ( application == null ) {
            return null;
        }

        ApplicationResponse applicationResponse = new ApplicationResponse();

        applicationResponse.setId( application.getId() );
        applicationResponse.setUserId( application.getUserId() );
        applicationResponse.setStatus( application.getStatus() );
        applicationResponse.setLoanAmount( application.getLoanAmount() );
        applicationResponse.setPurpose( application.getPurpose() );
        applicationResponse.setTenureMonths( application.getTenureMonths() );
        applicationResponse.setFirstName( application.getFirstName() );
        applicationResponse.setLastName( application.getLastName() );
        applicationResponse.setPhone( application.getPhone() );
        applicationResponse.setAddress( application.getAddress() );
        applicationResponse.setEmployerName( application.getEmployerName() );
        applicationResponse.setEmploymentType( application.getEmploymentType() );
        applicationResponse.setMonthlyIncome( application.getMonthlyIncome() );
        applicationResponse.setCreatedAt( application.getCreatedAt() );
        applicationResponse.setUpdatedAt( application.getUpdatedAt() );
        applicationResponse.setSubmittedAt( application.getSubmittedAt() );

        return applicationResponse;
    }

    @Override
    public LoanApplication toEntity(ApplicationRequest request) {
        if ( request == null ) {
            return null;
        }

        LoanApplication loanApplication = new LoanApplication();

        loanApplication.setLoanAmount( request.getLoanAmount() );
        loanApplication.setPurpose( request.getPurpose() );
        loanApplication.setTenureMonths( request.getTenureMonths() );
        loanApplication.setFirstName( request.getFirstName() );
        loanApplication.setLastName( request.getLastName() );
        loanApplication.setPhone( request.getPhone() );
        loanApplication.setAddress( request.getAddress() );
        loanApplication.setEmployerName( request.getEmployerName() );
        loanApplication.setEmploymentType( request.getEmploymentType() );
        loanApplication.setMonthlyIncome( request.getMonthlyIncome() );

        return loanApplication;
    }

    @Override
    public void updateEntity(ApplicationRequest request, LoanApplication application) {
        if ( request == null ) {
            return;
        }

        application.setLoanAmount( request.getLoanAmount() );
        application.setPurpose( request.getPurpose() );
        application.setTenureMonths( request.getTenureMonths() );
        application.setFirstName( request.getFirstName() );
        application.setLastName( request.getLastName() );
        application.setPhone( request.getPhone() );
        application.setAddress( request.getAddress() );
        application.setEmployerName( request.getEmployerName() );
        application.setEmploymentType( request.getEmploymentType() );
        application.setMonthlyIncome( request.getMonthlyIncome() );
    }
}
