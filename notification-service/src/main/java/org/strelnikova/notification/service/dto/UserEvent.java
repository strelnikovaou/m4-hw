package org.strelnikova.notification.service.dto;

public record UserEvent(String userName, String email, UserEventType status, java.time.Instant time){ }
