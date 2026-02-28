package org.strelnikova.notificationservice.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.strelnikova.notificationservice.entity.EmailOutbox;
import org.strelnikova.notificationservice.entity.EmailStatus;
import org.strelnikova.notificationservice.repository.EmailOutboxRepository;
import org.strelnikova.notificationservice.service.EmailService;
import org.strelnikova.notificationservice.service.NotificationMessageBuilder.EmailData;

import java.util.List;

@Component
@Slf4j
public class EmailOutboxScheduler {

    private final EmailOutboxRepository outboxRepository;
    private final EmailService emailService;

    private static final int BATCH_SIZE = 20;
    private static final int MAX_ATTEMPTS = 5;

    @Lazy
    private final EmailOutboxScheduler self;

    public EmailOutboxScheduler(EmailOutboxRepository outboxRepository,
                                EmailService emailService,
                                @Lazy EmailOutboxScheduler self) {
        this.outboxRepository = outboxRepository;
        this.emailService = emailService;
        this.self = self;
    }

    @Scheduled(fixedDelay = 10000) // каждые 10 секунд
    public void processOutbox() {
        log.debug("Starting outbox processing");

        List<EmailOutbox> pendingEmails = outboxRepository.findPendingEmailsForSend(BATCH_SIZE);

        for (EmailOutbox email : pendingEmails) {
            self.processEmail(email);
        }

        log.debug("Outbox processing finished, processed {} emails", pendingEmails.size());
    }

    @Transactional
    protected void processEmail(EmailOutbox email) {
        try {
            EmailData emailData = new EmailData(email.getToEmail(), email.getSubject(), email.getBody());
            emailService.sendEmail(emailData);

            markAsSent(email);
        } catch (Exception e) {
            log.warn("Failed to send email to {}, attempts: {}", email.getToEmail(), email.getAttempts(), e);
            handleFailure(email);
        }
    }

    private void markAsSent(EmailOutbox email) {
        email.setStatus(EmailStatus.SENT);
        outboxRepository.save(email);
    }

    private void handleFailure(EmailOutbox email) {
        int newAttempts = email.getAttempts() + 1;
        EmailStatus newStatus = newAttempts >= MAX_ATTEMPTS ? EmailStatus.FAILED : EmailStatus.PENDING;

        int updated = outboxRepository.updateStatusAndAttempts(
                email.getId(),
                newStatus,
                newAttempts,
                email.getVersion()
        );

        if (updated == 0) {
            log.warn("Concurrent modification detected for outbox id {}, skipping", email.getId());
        }
    }
}