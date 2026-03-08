package org.strelnikova.notification.service.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.strelnikova.notification.service.dto.UserEvent;
import org.strelnikova.notification.service.entity.EmailOutbox;
import org.strelnikova.notification.service.repository.EmailOutboxRepository;
import org.strelnikova.notification.service.service.NotificationMessageBuilder;
import org.strelnikova.notification.service.service.NotificationMessageBuilder.EmailData;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserEventKafkaListener {

    private final NotificationMessageBuilder messageBuilder;
    private final EmailOutboxRepository outboxRepository;

    @KafkaListener(topics = "${app.kafka.topic.user-events}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void listen(UserEvent event) {
        log.info("Received user event: {}", event);

        // Создаём данные письма
        EmailData emailData = messageBuilder.createEmailData(event);

        // Сохраняем в outbox
        EmailOutbox outbox = new EmailOutbox();
        outbox.setToEmail(emailData.to());
        outbox.setSubject(emailData.subject());
        outbox.setBody(emailData.body());
        outboxRepository.save(outbox);

        log.info("Saved outbox record for event: {}", event);
    }
}