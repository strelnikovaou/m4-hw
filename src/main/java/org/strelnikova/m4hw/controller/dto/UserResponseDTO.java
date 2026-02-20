package org.strelnikova.m4hw.controller.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponseDTO (UUID id, String name, String email, Integer age, OffsetDateTime createdAt){
}
