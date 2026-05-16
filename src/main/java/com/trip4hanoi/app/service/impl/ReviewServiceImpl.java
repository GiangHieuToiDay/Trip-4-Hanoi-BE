package com.trip4hanoi.app.service.impl;

import com.trip4hanoi.app.dto.req.ReviewRequest;
import com.trip4hanoi.app.dto.res.ReviewResponse;
import com.trip4hanoi.app.entity.Place;
import com.trip4hanoi.app.entity.Review;
import com.trip4hanoi.app.entity.User;
import com.trip4hanoi.app.exception.AppException;
import com.trip4hanoi.app.exception.ErrorCode;
import com.trip4hanoi.app.mapper.ReviewMapper;
import com.trip4hanoi.app.repository.PlaceRepository;
import com.trip4hanoi.app.repository.ReviewRepository;
import com.trip4hanoi.app.repository.UserRepository;
import com.trip4hanoi.app.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional
    public ReviewResponse createReview(ReviewRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        
        Place place = placeRepository.findById(request.getPlaceId())
                .orElseThrow(() -> new AppException(ErrorCode.PLACE_NOT_FOUND));

        Review review = reviewMapper.toReview(request);
        review.setUser(user);
        review.setPlace(place);

        Review saved = reviewRepository.save(review);
        return reviewMapper.toReviewResponse(saved);
    }

    @Override
    public List<ReviewResponse> getReviewsByPlace(Long placeId) {
        return reviewRepository.findByPlaceId(placeId).stream()
                .map(reviewMapper::toReviewResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getMyReviews() {
        Long userId = getCurrentUserId();
        return reviewRepository.findByUserId(userId).stream()
                .map(reviewMapper::toReviewResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }

    private Long getCurrentUserId() {
        var context = SecurityContextHolder.getContext();
        var authentication = context.getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            Object idClaim = jwt.getClaims().get("id");
            if (idClaim instanceof Number n) {
                return n.longValue();
            }
        }
        return 0L;
    }
}
