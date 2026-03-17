package com.trip4hanoi.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_event_follow")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEventFollow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(name = "notify_before_minutes")
    private Integer notifyBeforeMinutes;
}
