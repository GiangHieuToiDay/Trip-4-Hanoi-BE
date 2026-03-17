package com.trip4hanoi.app.dto.req;

import lombok.*;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CrossOrigin(origins = "http://localhost:5173")
public class ItineraryRequest {
    private String title;
    private Integer budget;
    private Integer days;
    private Integer numberOfPeople;
    private List<String> categoryNames;
}
