package com.trip4hanoi.app.dto.res.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationAnalyticsResponse {
    private Map<Integer, Long> chatVolumeByHour;
    private Map<String, Long> revenueGrowth;
    private List<String> aiTopKeywords;
}
