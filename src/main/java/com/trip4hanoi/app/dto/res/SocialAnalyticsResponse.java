package com.trip4hanoi.app.dto.res;


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
public class SocialAnalyticsResponse { // (Cộng đồng & Sự kiện)
    private List<PostEngagementDTO> topViralPosts;
    private Map<String, Long> postGrowthByMonth;// {"2024-04": 25, "2024-05": 40}
    private List<EventHotnessDTO> hotEvents;
}







