package org.strelnikova.userservice.model.outbox.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEventPayload {
    private String userName;
    private String email;
}