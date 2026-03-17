package com.trip4hanoi.app.dto.res;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponse {
    private Long id;
    private String name;
    private String description;
    private Long placeId;
    private String placeName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
