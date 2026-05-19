package com.trip4hanoi.app.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    private Long id;
    private Long reporterId;
    private String reporterName;
    private String reportType;
    private Long targetId;
    private String reason;
    private String status;
    private LocalDateTime createdAt;
}
