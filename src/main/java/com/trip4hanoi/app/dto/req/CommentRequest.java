package com.trip4hanoi.app.dto.req;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentRequest {
    private Long postId;
    private String content;
}
