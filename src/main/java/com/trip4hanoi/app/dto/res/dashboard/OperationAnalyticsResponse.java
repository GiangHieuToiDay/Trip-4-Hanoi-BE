package com.trip4hanoi.app.dto.res.dashboard;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationAnalyticsResponse {
    private Map<Integer, Long> chatVolumeByHour;
    private List<String> aiTopKeywords;
}
