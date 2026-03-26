package com.finflow.application.repository;

import com.finflow.application.entity.ApplicationStatus;
import com.finflow.application.entity.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<LoanApplication, Long> {


    List<LoanApplication> findByUserId(Long userId);

    List<LoanApplication> findByStatus(ApplicationStatus status);

    List<LoanApplication> findByStatusNot(ApplicationStatus status);
}