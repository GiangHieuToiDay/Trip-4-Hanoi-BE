package com.trip4hanoi.app.dto.res;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private Long id;
    private Long userId;
    private Long eventId;
    private String message;
    private String status;
    private LocalDateTime createdAt;
}
