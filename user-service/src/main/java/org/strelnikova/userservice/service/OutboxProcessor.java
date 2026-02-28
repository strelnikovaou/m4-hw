package org.strelnikova.userservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.strelnikova.userservice.model.UserEventType;
import org.strelnikova.userservice.model.outbox.OutboxEvent;
import org.strelnikova.userservice.model.outbox.OutboxStatus;
import org.strelnikova.userservice.model.outbox.dto.UserEventPayload;
import org.strelnikova.userservice.repository.OutboxEventRepository;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component

public class OutboxProcessor {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaUserService kafkaUserService;
    private final ObjectMapper objectMapper;
    private final OutboxProcessor self;

    public OutboxProcessor(OutboxEventRepository outboxEventRepository,
                           KafkaUserService kafkaUserService,
                           ObjectMapper objectMapper,
                           @Lazy OutboxProcessor self) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaUserService = kafkaUserService;
        this.objectMapper = objectMapper;
        this.self = self;
    }

    @Scheduled(fixedDelay = 5000)
    public void processOutboxEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findPendingEvents();
        log.info("Found {} pending outbox events", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                self.processSingleEvent(event.getId());
            } catch (Exception e) {
                log.error("Failed to process event {}: {}", event.getId(), e.getMessage());
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processSingleEvent(UUID eventId) {
        OutboxEvent event = outboxEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Outbox event not found: " + eventId));

        if (event.getStatus() != OutboxStatus.PENDING) {
            log.info("Event {} is no longer PENDING, skipping", eventId);
            return;
        }

        try {
            UserEventPayload payload = objectMapper.readValue(event.getPayload(), UserEventPayload.class);
            UserEventType eventType = UserEventType.valueOf(event.getEventType());

            kafkaUserService.sendUserEvent(payload.getUserName(), payload.getEmail(), eventType);

            event.setStatus(OutboxStatus.SENT);
            outboxEventRepository.save(event);
            log.info("Successfully processed outbox event {}", eventId);

        } catch (Exception e) {
            log.error("Error processing outbox event {}", eventId, e);

            int retryCount = event.getRetryCount() + 1;
            event.setRetryCount(retryCount);

            if (retryCount >= 5) {
                event.setStatus(OutboxStatus.FAILED);
                log.warn("Outbox event {} reached max retries, marking as FAILED", eventId);
            }

            outboxEventRepository.save(event);
        }
    }
}