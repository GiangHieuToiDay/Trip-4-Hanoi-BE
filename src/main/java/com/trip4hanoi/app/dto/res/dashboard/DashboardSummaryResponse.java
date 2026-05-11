package com.trip4hanoi.app.dto.res.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {
    private long totalUsers;
    private long totalPlaces;
    private long totalPosts;
    private long totalItineraries;
    private Map<String, Long> usersByRole;
    private double conversionRate;
    private List<LocationCoordinateDTO> heatmap;
}
