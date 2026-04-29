package com.trip4hanoi.app.dto.req;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreferenceRequest {
    private List<Long> categoryIds;
}
