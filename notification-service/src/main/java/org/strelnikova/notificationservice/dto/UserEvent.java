package org.strelnikova.notificationservice.dto;

public record UserEvent(String userName, String email, UserEventType status, java.time.Instant time){ }
