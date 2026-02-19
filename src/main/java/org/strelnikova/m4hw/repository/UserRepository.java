package org.strelnikova.m4hw.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.strelnikova.m4hw.model.User;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);
}
