package com.trip4hanoi.app.service;

import com.trip4hanoi.app.dto.res.dashboard.*;

public interface DashboardService {
    DashboardSummaryResponse getSummary();
    PlaceAnalyticsResponse getPlaceAnalytics();
    SocialAnalyticsResponse getSocialAnalytics();
    ItineraryAnalyticsResponse getItineraryAnalytics();
    OperationAnalyticsResponse getOperationAnalytics();
}
