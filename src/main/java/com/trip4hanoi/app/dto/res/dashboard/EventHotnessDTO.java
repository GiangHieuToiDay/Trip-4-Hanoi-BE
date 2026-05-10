package com.trip4hanoi.app.dto.res.dashboard;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventHotnessDTO {
    private Long eventId;
    private String name;
    private long hotnessScore;
    private String status;
}
