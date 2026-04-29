package com.trip4hanoi.app.dto.req;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostRequest {
    private String content;
    private List<String> imageUrls;
    private List<Long> taggedPlaceIds;
}
