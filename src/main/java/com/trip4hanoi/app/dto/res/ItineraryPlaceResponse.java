package com.trip4hanoi.app.dto.res;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryPlaceResponse {
    private Long id;
    private Long placeId;
    private String placeName;
    private Integer dayNumber;
    private Integer orderIndex;
    private String session;
    private Integer estimatedCost;
}
