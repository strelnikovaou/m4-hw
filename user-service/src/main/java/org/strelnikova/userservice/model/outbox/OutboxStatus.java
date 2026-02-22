package org.strelnikova.userservice.model.outbox;

public enum OutboxStatus {
    PENDING,
    SENT,
    FAILED
}