package com.trip4hanoi.app.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostEngagementDTO {
    private Long postId;
    private String title;
    private String author;
    private double viralRate; // (likes + saves) / views
}
