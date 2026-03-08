package org.strelnikova.user.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.strelnikova.user.service.model.User;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);
}
