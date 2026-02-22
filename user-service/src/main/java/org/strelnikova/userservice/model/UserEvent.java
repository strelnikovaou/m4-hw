package org.strelnikova.userservice.model;

public record UserEvent(String userName, String email, UserEventType status, java.time.Instant time){ }
