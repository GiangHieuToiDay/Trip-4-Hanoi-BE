package com.trip4hanoi.app.mapper;

import com.trip4hanoi.app.dto.req.PostRequest;
import com.trip4hanoi.app.dto.res.PostImageResponse;
import com.trip4hanoi.app.dto.res.PostResponse;
import com.trip4hanoi.app.dto.res.TaggedPlaceResponse;
import com.trip4hanoi.app.entity.Place;
import com.trip4hanoi.app.entity.Post;
import com.trip4hanoi.app.entity.PostImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface PostMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.actualUsername", target = "username")
    @Mapping(source = "user.avatar", target = "userAvatar")
    @Mapping(source = "images", target = "images")
    @Mapping(source = "places", target = "taggedPlaces", qualifiedByName = "mapPlacesToResponses")
    @Mapping(source = "status", target = "status")
    @Mapping(target = "likeCount", expression = "java(post.getLikes() != null ? post.getLikes().size() : 0)")
    @Mapping(target = "commentCount", expression = "java(post.getComments() != null ? post.getComments().size() : 0)")
    @Mapping(target = "isLiked", ignore = true)
    PostResponse toPostResponse(Post post);

    PostImageResponse toPostImageResponse(PostImage postImage);

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
    Post toPost(PostRequest request);

    @Named("mapPlacesToResponses")
    default List<TaggedPlaceResponse> mapPlacesToResponses(List<Place> places) {
        if (places == null) return null;
        return places.stream()
                .map(p -> TaggedPlaceResponse.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .build())
                .collect(Collectors.toList());
    }
}
