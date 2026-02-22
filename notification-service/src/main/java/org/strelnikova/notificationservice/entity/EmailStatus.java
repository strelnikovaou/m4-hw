package org.strelnikova.notificationservice.entity;

public enum EmailStatus {
    PENDING,    // ожидает отправки
    SENT,       // успешно отправлено
    FAILED      // окончательная неудача
}