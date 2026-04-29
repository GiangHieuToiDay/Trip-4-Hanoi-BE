package com.trip4hanoi.app.dto.res;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreferenceResponse {
    private Long id;
    private Long userId;
    private Long categoryId;
    private String categoryName;
}
