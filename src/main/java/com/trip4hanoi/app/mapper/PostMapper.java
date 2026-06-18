package com.trip4hanoi.app.mapper;

import com.trip4hanoi.app.dto.req.PostRequest;
import com.trip4hanoi.app.dto.res.PostImageResponse;
import com.trip4hanoi.app.dto.res.PostResponse;
import com.trip4hanoi.app.dto.res.TaggedPlaceResponse;
import com.trip4hanoi.app.entity.Place;
import com.trip4hanoi.app.entity.Post;
import com.trip4hanoi.app.entity.PostImage;
import com.trip4hanoi.app.repository.PostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class PostMapper {


    @Autowired
    protected PostLikeRepository postLikeRepository;

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.actualUsername", target = "username")
    @Mapping(source = "user.avatar", target = "userAvatar")
    @Mapping(source = "images", target = "images")
    @Mapping(source = "places", target = "taggedPlaces", qualifiedByName = "mapPlacesToResponses")
    @Mapping(source = "status", target = "status")
    @Mapping(target = "likeCount", expression = "java(post.getLikes() != null ? post.getLikes().size() : 0)")
    @Mapping(target = "commentCount", expression = "java(post.getComments() != null ? post.getComments().size() : 0)")
    @Mapping(target = "isLiked", ignore = true)
    public abstract PostResponse toPostResponse(Post post);

    @AfterMapping
    protected void fillIsLiked(Post post, @MappingTarget PostResponse response) {
        Long userId = getCurrentUserId();
        if (userId > 0) {
            response.setIsLiked(postLikeRepository.existsByPostIdAndUserId(post.getId(), userId));
        } else {
            response.setIsLiked(false);
        }
    }

    private Long getCurrentUserId() {
        var context = SecurityContextHolder.getContext();
        if (context == null) return 0L;
        var auth = context.getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            return (Long) jwt.getClaims().get("id");
        }
        return 0L;
    }

    public abstract PostImageResponse toPostImageResponse(PostImage postImage);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "likes", ignore = true)
    @Mapping(target = "saves", ignore = true)
    @Mapping(target = "places", ignore = true)
    public abstract Post toPost(PostRequest request);

    @Named("mapPlacesToResponses")
    protected List<TaggedPlaceResponse> mapPlacesToResponses(List<Place> places) {
        if (places == null) return null;
        return places.stream()
                .map(p -> TaggedPlaceResponse.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .build())
                .collect(Collectors.toList());
    }
}
