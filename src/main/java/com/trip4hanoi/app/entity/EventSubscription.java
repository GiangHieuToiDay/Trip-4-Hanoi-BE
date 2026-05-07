package com.trip4hanoi.app.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "event_subscription",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_sub",
                        columnNames = {"user_id", "event_id", "notify_before_minutes"}
                )
        }
)
@Data
public class EventSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long eventId;

    // phút trước khi notify (vd: 4320 = 3 ngày)
    private Integer notifyBeforeMinutes;

    private boolean isNotified;

    private int retryCount;

    private LocalDateTime lastAttemptAt;
}
