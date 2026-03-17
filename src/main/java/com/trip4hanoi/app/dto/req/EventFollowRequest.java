package com.trip4hanoi.app.dto.req;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventFollowRequest {
    private Long eventId;
    private Integer notifyBeforeMinutes;
}
