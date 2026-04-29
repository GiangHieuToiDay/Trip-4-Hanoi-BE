package com.trip4hanoi.app.mapper;

import com.trip4hanoi.app.dto.req.ReviewRequest;
import com.trip4hanoi.app.dto.res.ReviewResponse;
import com.trip4hanoi.app.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "place.id", target = "placeId")
    @Mapping(source = "place.name", target = "placeName")
    ReviewResponse toReviewResponse(Review review);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "place", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Review toReview(ReviewRequest request);
}
