package com.trip4hanoi.app.dto.res;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {
    private Long id;
    private Long userId;
    private String userName;
    private Long placeId;
    private String placeName;
    private Integer rating;
    private String comment;
    private String imageUrl;
    private LocalDateTime createdAt;
}
