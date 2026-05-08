package com.trip4hanoi.app.dto.res;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime updatedAt;

    private Integer likeCount;
    private Integer commentCount;
    private Boolean isLiked;
    private List<PostImageResponse> images;
    private List<Long> taggedPlaceIds;
}
