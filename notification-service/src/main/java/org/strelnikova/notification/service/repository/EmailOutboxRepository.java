package org.strelnikova.notification.service.repository;

import org.strelnikova.notification.service.entity.EmailOutbox;
import org.strelnikova.notification.service.entity.EmailStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface EmailOutboxRepository extends JpaRepository<EmailOutbox, Long> {

    @Query(value = """
        SELECT * FROM email_outbox
        WHERE status = 'PENDING' AND attempts < 5
        ORDER BY created_at
        LIMIT :limit
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    List<EmailOutbox> findPendingEmailsForSend(@Param("limit") int limit);

    @Modifying
    @Transactional
    @Query("UPDATE EmailOutbox e SET e.status = :status, e.attempts = :attempts WHERE e.id = :id AND e.version = :version")
    int updateStatusAndAttempts(@Param("id") Long id,
                                @Param("status") EmailStatus status,
                                @Param("attempts") int attempts,
                                @Param("version") int version);
}