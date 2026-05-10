package com.trip4hanoi.app.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationCoordinateDTO {
    private Double lat;
    private Double lng;
}
