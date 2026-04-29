package com.trip4hanoi.app.dto.res;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {
    private Long id;
    private Long postId;
    private Long userId;
    private String userName;
    private String content;
    private LocalDateTime createdAt;
}
