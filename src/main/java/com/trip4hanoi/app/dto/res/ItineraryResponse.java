package com.trip4hanoi.app.dto.res;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryResponse {
    private Long id;
    private String title;
    private Integer budget;
    private Integer days;
    private Integer numberOfPeople;
    private LocalDateTime createdAt;
    private List<DayItineraryResponse> itineraryDays;
}
