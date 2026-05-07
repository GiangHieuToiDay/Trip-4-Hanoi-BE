package com.trip4hanoi.app.repository;


import com.trip4hanoi.app.entity.EventSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventSubscriptionRepository extends JpaRepository<EventSubscription, Long> {
    @Query(value = """
            SELECT es.*
            FROM event_subscription es
            JOIN events e ON es.event_id = e.id
            WHERE es.is_notified = false
            AND e.deleted = false
            AND (e.start_time - INTERVAL es.notify_before_minutes MINUTE) <= :now
            LIMIT 100
            """, nativeQuery = true)
    List<EventSubscription> findDueNotifications(@Param("now") LocalDateTime now);

    List<EventSubscription> findByEventId(Long eventId);

    boolean existsByUserIdAndEventIdAndNotifyBeforeMinutes(Long userId, Long eventId, Integer notifyBeforeMinutes);
}
