package org.strelnikova.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.strelnikova.userservice.controller.UserMapper;
import org.strelnikova.userservice.model.UserEventType;
import org.strelnikova.userservice.model.UserEvent;
import org.strelnikova.userservice.repository.UserRepository;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaUserService {
    private final KafkaTemplate<String, UserEvent> kafkaTemplate;

    @Value("${app.kafka.topic.user-events}")
    private String userEventsTopic;

    public void sendUserEvent(String userName , String email, UserEventType eventType) {
        UserEvent event = new UserEvent(userName, email, eventType, Instant.now());
        try {
            SendResult<String, UserEvent> result = kafkaTemplate
                    .send(userEventsTopic, userName, event)
                    .get(5, TimeUnit.SECONDS);

            log.info("Sent event: {} to topic {}, partition {}, offset {}",
                    event, userEventsTopic,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());

        } catch (Exception e) {
            log.error("Failed to send event: {}", event, e);
            throw new RuntimeException("Kafka send failed", e);
        }
    }
}
