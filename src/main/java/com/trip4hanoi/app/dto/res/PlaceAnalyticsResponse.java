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
public class PlaceAnalyticsResponse { //(Địa điểm & Xu hướng
    private List<PlaceSoreDTO> top10Places;
    private List<PlaceSoreDTO> abandonedPlaces;
    private Map<String, Double> sentimentByUser;// Category -> Avg Rating
}


