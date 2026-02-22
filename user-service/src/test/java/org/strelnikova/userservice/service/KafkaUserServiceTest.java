package org.strelnikova.userservice.service;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;
import org.strelnikova.userservice.model.UserEvent;
import org.strelnikova.userservice.model.UserEventType;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование KafkaUserService. Проверяет успешную отправку и обработку ошибок")
class KafkaUserServiceTest {

    @Mock
    private KafkaTemplate<String, UserEvent> kafkaTemplate;

    @InjectMocks
    private KafkaUserService kafkaUserService;

    private final String testTopic = "test-topic";
    private final String userName = "testUser";
    private final String email = "test@example.com";
    private final UserEventType eventType = UserEventType.CREATED;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(kafkaUserService, "userEventsTopic", testTopic);
    }

    @Test
    @DisplayName("Событие UserEvent должно успешно отсылаться.")
    void shouldSendEventSuccessfully() throws Exception {

        CompletableFuture<SendResult<String, UserEvent>> future = new CompletableFuture<>();
        ProducerRecord<String, UserEvent> producerRecord = new ProducerRecord<>(testTopic, userName, null);
        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition(testTopic, 0),
                0L, 0, 0L, 0, 0
        );
        SendResult<String, UserEvent> sendResult = new SendResult<>(producerRecord, metadata);
        future.complete(sendResult);

        when(kafkaTemplate.send(eq(testTopic), eq(userName), any(UserEvent.class)))
                .thenReturn(future);


        kafkaUserService.sendUserEvent(userName, email, eventType);

        ArgumentCaptor<UserEvent> eventCaptor = ArgumentCaptor.forClass(UserEvent.class);
        verify(kafkaTemplate).send(eq(testTopic), eq(userName), eventCaptor.capture());

        UserEvent sentEvent = eventCaptor.getValue();
        assertThat(sentEvent.userName()).isEqualTo(userName);
        assertThat(sentEvent.email()).isEqualTo(email);
        assertThat(sentEvent.status()).isEqualTo(eventType);
        assertThat(sentEvent.time()).isNotNull();
    }

    @Test
    @DisplayName("Должно выбрасываться исключение при ошибки отсылки")
    void shouldThrowExceptionWhenSendFails() {

        CompletableFuture<SendResult<String, UserEvent>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka unavailable"));

        when(kafkaTemplate.send(eq(testTopic), eq(userName), any(UserEvent.class)))
                .thenReturn(future);

        assertThatThrownBy(() -> kafkaUserService.sendUserEvent(userName, email, eventType))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Kafka send failed");

        verify(kafkaTemplate).send(eq(testTopic), eq(userName), any(UserEvent.class));
    }

    @Test
    @DisplayName("Должно выбрасываться исключение по таймауту")
    void shouldThrowExceptionOnTimeout() {

        CompletableFuture<SendResult<String, UserEvent>> future = new CompletableFuture<>();

        when(kafkaTemplate.send(eq(testTopic), eq(userName), any(UserEvent.class)))
                .thenReturn(future);

        assertThatThrownBy(() -> kafkaUserService.sendUserEvent(userName, email, eventType))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Kafka send failed");

        verify(kafkaTemplate).send(eq(testTopic), eq(userName), any(UserEvent.class));
    }
}