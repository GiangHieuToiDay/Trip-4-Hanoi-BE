package com.trip4hanoi.app.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventHotnessDTO {
    private Long eventId;
    private String name;
    private long hotnessScore; // Tổng Follow + Subscription
    private String status;      // UPCOMING, HAPPENING, ENDED
}
