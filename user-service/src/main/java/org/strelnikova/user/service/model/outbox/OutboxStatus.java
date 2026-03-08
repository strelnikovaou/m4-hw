package org.strelnikova.user.service.model.outbox;

public enum OutboxStatus {
    PENDING,
    SENT,
    FAILED
}