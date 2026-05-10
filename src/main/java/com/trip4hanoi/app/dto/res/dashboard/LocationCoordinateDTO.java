package com.trip4hanoi.app.dto.res.dashboard;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationCoordinateDTO {
    private Double lat;
    private Double lng;
}
