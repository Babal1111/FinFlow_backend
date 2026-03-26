package com.finflow.admin.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "decisions")
public class Decision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long applicationId;

    @Column(nullable = false)
    private Long adminId;

    @Column(nullable = false)
    private String decision;    // APPROVED / REJECTED

    private String remarks;
    private Double approvedAmount;
    private Integer tenureMonths;
    private Double interestRate;

    @Column(updatable = false)
    private LocalDateTime decidedAt;

    @PrePersist
    public void prePersist() {
        this.decidedAt = LocalDateTime.now();
    }
}