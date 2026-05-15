package com.trip4hanoi.app.dto.req;

import com.trip4hanoi.app.dto.res.ScheduleItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveAIItineraryRequest {
    private String title;
    private List<ScheduleItem> timeline;
}
