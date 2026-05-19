package com.trip4hanoi.app.controller;

import com.trip4hanoi.app.dto.res.APIResponse;
import com.trip4hanoi.app.dto.res.dashboard.*;
import com.trip4hanoi.app.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<APIResponse<DashboardSummaryResponse>> getSummary() {
        return ResponseEntity.ok(APIResponse.<DashboardSummaryResponse>builder()
                .status(200)
                .code(1000)
                .data(dashboardService.getSummary())
                .build());
    }

    @GetMapping("/places")
    public ResponseEntity<APIResponse<PlaceAnalyticsResponse>> getPlaceAnalytics() {
        return ResponseEntity.ok(APIResponse.<PlaceAnalyticsResponse>builder()
                .status(200)
                .code(1000)
                .data(dashboardService.getPlaceAnalytics())
                .build());
    }

    @GetMapping("/itinerary")
    public ResponseEntity<APIResponse<ItineraryAnalyticsResponse>> getItineraryAnalytics() {
        return ResponseEntity.ok(APIResponse.<ItineraryAnalyticsResponse>builder()
                .status(200)
                .code(1000)
                .data(dashboardService.getItineraryAnalytics())
                .build());
    }

    @GetMapping("/social")
    public ResponseEntity<APIResponse<SocialAnalyticsResponse>> getSocialAnalytics() {
        return ResponseEntity.ok(APIResponse.<SocialAnalyticsResponse>builder()
                .status(200)
                .code(1000)
                .data(dashboardService.getSocialAnalytics())
                .build());
    }

    @GetMapping("/operations")
    public ResponseEntity<APIResponse<OperationAnalyticsResponse>> getOperationAnalytics() {
        return ResponseEntity.ok(APIResponse.<OperationAnalyticsResponse>builder()
                .status(200)
                .code(1000)
                .data(dashboardService.getOperationAnalytics())
                .build());
    }
}
