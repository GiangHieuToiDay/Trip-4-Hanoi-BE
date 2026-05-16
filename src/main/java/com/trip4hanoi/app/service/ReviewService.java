package com.trip4hanoi.app.service;

import com.trip4hanoi.app.dto.req.ReviewRequest;
import com.trip4hanoi.app.dto.res.ReviewResponse;
import java.util.List;

public interface ReviewService {
    ReviewResponse createReview(ReviewRequest request, Long userId);
    List<ReviewResponse> getReviewsByPlace(Long placeId);
    void deleteReview(Long reviewId);
}
