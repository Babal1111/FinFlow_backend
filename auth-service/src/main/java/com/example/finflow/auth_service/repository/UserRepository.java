package com.example.finflow.auth_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.finflow.auth_service.entity.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    public Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    //This is Spring Data JPA  ---)
    //No implementation needed
    //Method name = query LOOK further in notes

}
