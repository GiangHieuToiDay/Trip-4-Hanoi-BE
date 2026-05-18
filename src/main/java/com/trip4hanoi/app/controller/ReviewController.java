package com.trip4hanoi.app.controller;

import com.trip4hanoi.app.dto.req.ReviewRequest;
import com.trip4hanoi.app.dto.res.APIResponse;
import com.trip4hanoi.app.dto.res.PageResponse;
import com.trip4hanoi.app.dto.res.ReviewResponse;
import com.trip4hanoi.app.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<APIResponse<ReviewResponse>> createReview(
            @RequestBody ReviewRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("id");
        ReviewResponse review = reviewService.createReview(request, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                APIResponse.<ReviewResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .code(1000)
                        .message("Successfully created review")
                        .data(review)
                        .build()
        );
    }

    @GetMapping("/place/{placeId}")
    public ResponseEntity<APIResponse<List<ReviewResponse>>> getReviewsByPlace(@PathVariable Long placeId) {
        List<ReviewResponse> reviews = reviewService.getReviewsByPlace(placeId);

        return ResponseEntity.ok(
                APIResponse.<List<ReviewResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .code(1000)
                        .message("Successfully retrieved reviews")
                        .data(reviews)
                        .build()
        );
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<APIResponse<List<ReviewResponse>>> getMyReviews() {
        List<ReviewResponse> reviews = reviewService.getMyReviews();

        return ResponseEntity.ok(
                APIResponse.<List<ReviewResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .code(1000)
                        .message("Successfully retrieved my reviews")
                        .data(reviews)
                        .build()
        );
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('MODERATE_CONTENT')")
    public ResponseEntity<APIResponse<PageResponse<ReviewResponse>>> getAllReviews(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String keyword
    ) {
        PageResponse<ReviewResponse> reviews = reviewService.getAllReviews(page, size, rating, keyword);

        return ResponseEntity.ok(
                APIResponse.<PageResponse<ReviewResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .code(1000)
                        .message("Successfully retrieved all reviews")
                        .data(reviews)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MODERATE_CONTENT') or isAuthenticated()")
    public ResponseEntity<APIResponse<Void>> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);

        return ResponseEntity.ok(
                APIResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .code(1000)
                        .message("Successfully deleted review")
                        .build()
        );
    }
}
