package com.trip4hanoi.app.dto.req;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequest {
    private Long placeId;
    private Integer rating;
    private String comment;
    private String imageUrl;
}
