package org.strelnikova.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.strelnikova.userservice.model.User;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);
}
