package com.trip4hanoi.app.dto.req;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryPlaceRequest {
    private Long itineraryId;
    private Long placeId;
    private Integer dayNumber;
    private Integer orderIndex;
    private Integer estimatedCost;
}
