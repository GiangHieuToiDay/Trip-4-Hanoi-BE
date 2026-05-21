package com.trip4hanoi.app.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleItem {
    private String time;
    private String activity;
    private Long placeId;
    private String note;
    private String estimatedCost;
}
