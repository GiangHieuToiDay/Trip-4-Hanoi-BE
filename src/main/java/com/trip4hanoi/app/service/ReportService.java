package com.trip4hanoi.app.service;

import com.trip4hanoi.app.dto.req.ReportRequest;
import com.trip4hanoi.app.dto.res.ReportResponse;

import java.util.List;

public interface ReportService {
    ReportResponse createReport(Long reporterId, ReportRequest request);
    List<ReportResponse> getAllReports();
    List<ReportResponse> getPendingReports();
    ReportResponse updateReportStatus(Long id, String status);
}
