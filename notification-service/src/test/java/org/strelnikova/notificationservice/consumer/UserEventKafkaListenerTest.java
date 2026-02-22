package org.strelnikova.notificationservice.consumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.strelnikova.notificationservice.dto.UserEvent;
import org.strelnikova.notificationservice.dto.UserEventType;
import org.strelnikova.notificationservice.entity.EmailOutbox;
import org.strelnikova.notificationservice.entity.EmailStatus;
import org.strelnikova.notificationservice.repository.EmailOutboxRepository;
import org.strelnikova.notificationservice.service.NotificationMessageBuilder;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты UserEventKafkaListener")
class UserEventKafkaListenerTest {

    @Mock
    private NotificationMessageBuilder messageBuilder;

    @Mock
    private EmailOutboxRepository outboxRepository;

    @InjectMocks
    private UserEventKafkaListener listener;

    @Captor
    private ArgumentCaptor<EmailOutbox> outboxCaptor;

    @Test
    @DisplayName("Событие UserEvent должно сохраняться в outbox корректно")
    void listen_shouldSaveOutboxRecordWithCorrectData() {

        UserEvent event = new UserEvent(
                "john_doe",
                "john@example.com",
                UserEventType.CREATED,
                Instant.now()
        );

        NotificationMessageBuilder.EmailData emailData = new NotificationMessageBuilder.EmailData(
                "john@example.com",
                "Subject",
                "Body"
        );

        when(messageBuilder.createEmailData(event)).thenReturn(emailData);

        listener.listen(event);

        verify(messageBuilder).createEmailData(event);
        verify(outboxRepository).save(outboxCaptor.capture());

        EmailOutbox savedOutbox = outboxCaptor.getValue();
        assertThat(savedOutbox.getToEmail()).isEqualTo("john@example.com");
        assertThat(savedOutbox.getSubject()).isEqualTo("Subject");
        assertThat(savedOutbox.getBody()).isEqualTo("Body");
        assertThat(savedOutbox.getStatus()).isEqualTo(EmailStatus.PENDING);
        assertThat(savedOutbox.getAttempts()).isZero();
    }
}