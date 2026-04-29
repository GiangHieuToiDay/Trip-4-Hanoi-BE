package com.trip4hanoi.app.dto.res;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostSaveResponse {
    private Long id;
    private Long postId;
    private Long userId;
}
