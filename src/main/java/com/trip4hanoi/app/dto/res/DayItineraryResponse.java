package com.trip4hanoi.app.dto.res;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DayItineraryResponse {
    private Integer dayNumber;
    private List<ItineraryPlaceResponse> places;
}
