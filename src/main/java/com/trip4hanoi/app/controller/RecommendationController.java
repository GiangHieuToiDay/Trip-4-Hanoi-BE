package com.trip4hanoi.app.controller;

import com.trip4hanoi.app.dto.res.APIResponse;
import com.trip4hanoi.app.dto.res.PlaceResponse;
import com.trip4hanoi.app.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<APIResponse<List<PlaceResponse>>> getRecommendations(
            @RequestParam(defaultValue = "6") int limit) {
        
        List<PlaceResponse> result = recommendationService.getPersonalizedRecommendations(limit);

        return ResponseEntity.ok(APIResponse.<List<PlaceResponse>>builder()
                .status(200)
                .code(1000)
                .message("Successfully retrieved personalized recommendations")
                .data(result)
                .build());
    }
}
