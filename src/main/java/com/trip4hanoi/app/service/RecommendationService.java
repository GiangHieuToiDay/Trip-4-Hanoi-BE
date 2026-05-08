package com.trip4hanoi.app.service;

import com.trip4hanoi.app.dto.res.PlaceResponse;
import java.util.List;

public interface RecommendationService {
    List<PlaceResponse> getPersonalizedRecommendations(int limit);
}
