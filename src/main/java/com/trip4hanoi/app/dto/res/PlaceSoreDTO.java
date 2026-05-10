package com.trip4hanoi.app.dto.res;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceSoreDTO {
    private Long placeId;
    private String name;
    private long score; // Tính bằng công thức (saves * 2 + reviews)
    private int viewCount;
    private double ratingAvg;
}
