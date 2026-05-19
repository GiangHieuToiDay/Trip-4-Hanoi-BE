package com.trip4hanoi.app.controller;

import com.trip4hanoi.app.dto.req.ReportRequest;
import com.trip4hanoi.app.dto.res.APIResponse;
import com.trip4hanoi.app.dto.res.ReportResponse;
import com.trip4hanoi.app.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j(topic = "REPORT-CONTROLLER")
public class ReportController {

    private final ReportService reportService;

    // Người dùng báo cáo một bài viết / comment / user
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<APIResponse<ReportResponse>> createReport(
            @Valid @RequestBody ReportRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        
        Long userId = jwt.getClaim("id");
        ReportResponse response = reportService.createReport(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(APIResponse.<ReportResponse>builder()
                .status(HttpStatus.CREATED.value())
                .code(1000)
                .message("Report submitted successfully")
                .data(response)
                .build());
    }

    // Admin lấy tất cả báo cáo
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<List<ReportResponse>>> getAllReports() {
        List<ReportResponse> reports = reportService.getAllReports();
        return ResponseEntity.ok(APIResponse.<List<ReportResponse>>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .data(reports)
                .build());
    }

    // Admin lấy các báo cáo đang chờ xử lý
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<List<ReportResponse>>> getPendingReports() {
        List<ReportResponse> reports = reportService.getPendingReports();
        return ResponseEntity.ok(APIResponse.<List<ReportResponse>>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .data(reports)
                .build());
    }

    // Admin xử lý báo cáo (VD: đổi từ PENDING -> RESOLVED / DISMISSED)
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<ReportResponse>> updateReportStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        
        ReportResponse response = reportService.updateReportStatus(id, status);
        return ResponseEntity.ok(APIResponse.<ReportResponse>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Report status updated successfully")
                .data(response)
                .build());
    }
}
