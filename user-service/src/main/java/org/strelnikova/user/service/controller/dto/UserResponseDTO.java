package org.strelnikova.user.service.controller.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponseDTO (UUID id, String name, String email, Integer age, OffsetDateTime createdAt){
}
