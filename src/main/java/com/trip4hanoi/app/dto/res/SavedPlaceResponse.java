package com.trip4hanoi.app.dto.res;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedPlaceResponse {
    private Long id;
    private Long userId;
    private PlaceResponse place;
}
