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
public class PlaceAnalyticsResponse {
    private List<PlaceScoreDTO> top10Places;
    private List<PlaceScoreDTO> abandonedPlaces;
    private Map<String, Double> sentimentByCategory;
}
