package com.trip4hanoi.app.dto.res.dashboard;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostEngagementDTO {
    private Long postId;
    private String title;
    private String author;
    private double viralRate;
}
