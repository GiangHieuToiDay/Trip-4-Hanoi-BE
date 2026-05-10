package com.trip4hanoi.app.controller;

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
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    @GetMapping("/places")
    public ResponseEntity<PlaceAnalyticsResponse> getPlaceAnalytics() {
        return ResponseEntity.ok(dashboardService.getPlaceAnalytics());
    }

    @GetMapping("/itinerary")
    public ResponseEntity<ItineraryAnalyticsResponse> getItineraryAnalytics() {
        return ResponseEntity.ok(dashboardService.getItineraryAnalytics());
    }

    @GetMapping("/social")
    public ResponseEntity<SocialAnalyticsResponse> getSocialAnalytics() {
        return ResponseEntity.ok(dashboardService.getSocialAnalytics());
    }

    @GetMapping("/operations")
    public ResponseEntity<OperationAnalyticsResponse> getOperationAnalytics() {
        return ResponseEntity.ok(dashboardService.getOperationAnalytics());
    }
}
