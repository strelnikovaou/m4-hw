package org.strelnikova.notificationservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.strelnikova.notificationservice.dto.UserEvent;

@Component
@RequiredArgsConstructor
public class NotificationMessageBuilder {

    private final MessageSource messageSource;

    public EmailData createEmailData(UserEvent event) {
        String subject = messageSource.getMessage("notification.subject", null, LocaleContextHolder.getLocale());

        String statusKey = switch (event.status()) {
            case CREATED -> "notification.status.created";
            case UPDATED -> "notification.status.updated";
            case DELETED -> "notification.status.deleted";
        };
        String statusText = messageSource.getMessage(statusKey, null, LocaleContextHolder.getLocale());

        String body = messageSource.getMessage("notification.body",
                new Object[]{event.userName(), event.userName(), statusText},
                LocaleContextHolder.getLocale());

        return new EmailData(event.email(), subject, body);
    }

    public record EmailData(String to, String subject, String body) {}
}