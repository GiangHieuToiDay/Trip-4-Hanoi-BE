package com.trip4hanoi.app.dto.res.dashboard;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlaceScoreDTO {
    private Long placeId;
    private String name;
    private long score;
    private int viewCount;
    private double ratingAvg;
}
