package com.trip4hanoi.app.dto.res;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEventFollowResponse {
    private Long id;
    private Long userId;
    private Long eventId;
    private String eventName;
    private Integer notifyBeforeMinutes;
}
