package org.strelnikova.notification.service.entity;

public enum EmailStatus {
    PENDING,    // ожидает отправки
    SENT,       // успешно отправлено
    FAILED      // окончательная неудача
}