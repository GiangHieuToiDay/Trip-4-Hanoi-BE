package com.trip4hanoi.app.dto.req;

import lombok.*;
import java.util.List;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreferenceRequest {
    private List<Long> categoryIds;
}
