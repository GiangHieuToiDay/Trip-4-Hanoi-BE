package com.trip4hanoi.app.dto.res.dashboard;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryAnalyticsResponse {
    private double avgTripDuration;
    private double avgCompletionRate;
}
