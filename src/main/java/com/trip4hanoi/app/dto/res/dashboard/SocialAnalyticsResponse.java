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
public class SocialAnalyticsResponse {
    private List<PostEngagementDTO> topViralPosts;
    private Map<String, Long> postGrowthByMonth;
    private List<EventHotnessDTO> hotEvents;
}
