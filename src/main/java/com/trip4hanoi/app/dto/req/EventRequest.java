package com.trip4hanoi.app.dto.req;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRequest {
    private String name;
    private String description;
    private Long placeId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
