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
public class ItineraryUpdateFullRequest {
    private Long id;
    private String title;
    private Integer budget;
    private Integer days;
    private Integer numberOfPeople;

    private List<ItineraryDayRequest> itineraryDays;
}
