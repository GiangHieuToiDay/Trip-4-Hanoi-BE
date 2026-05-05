package com.trip4hanoi.app.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryDayRequest {
    private Integer dayNumber;
    private List<ItineraryPlaceRequest> places;
}
