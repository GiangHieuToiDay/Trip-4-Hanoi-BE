package com.trip4hanoi.app.dto.res;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String title;
    private String content;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean isLiked;
    private List<PostImageResponse> images;
    private List<Long> taggedPlaceIds;
}
