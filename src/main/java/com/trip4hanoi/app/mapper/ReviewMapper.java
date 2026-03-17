package com.trip4hanoi.app.mapper;

import com.trip4hanoi.app.dto.req.ReviewRequest;
import com.trip4hanoi.app.dto.res.ReviewResponse;
import com.trip4hanoi.app.entity.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {
    public ReviewResponse toReviewResponse(Review review) {
        if (review == null) return null;
        return ReviewResponse.builder()
                .id(review.getId())
                .userName(review.getUser() != null ? review.getUser().getName() : "Anonymous")
                .rating(review.getRating())
                .comment(review.getComment())
                .imageUrl(review.getImageUrl())
                .createdAt(review.getCreatedAt())
                .build();
    }

    public Review toReviewEntity(ReviewRequest request) {
        if (request == null) return null;
        return Review.builder()
                .rating(request.getRating())
                .comment(request.getComment())
                .imageUrl(request.getImageUrl())
                .build();
    }
}
