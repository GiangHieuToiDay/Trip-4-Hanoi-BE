package com.trip4hanoi.app.dto.res;
import com.fasterxml.jackson.annotation.JsonFormat;
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
    private String username;
    private String avatar;
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime createdAt;
}
