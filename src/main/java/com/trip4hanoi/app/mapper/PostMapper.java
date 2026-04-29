package com.trip4hanoi.app.mapper;

import com.trip4hanoi.app.dto.req.PostRequest;
import com.trip4hanoi.app.dto.res.PostResponse;
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
    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "images", target = "imageUrls", qualifiedByName = "mapImagesToUrls")
    @Mapping(source = "places", target = "taggedPlaceIds", qualifiedByName = "mapPlacesToIds")
    PostResponse toPostResponse(Post post);

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

    @Named("mapImagesToUrls")
    default List<String> mapImagesToUrls(List<PostImage> images) {
        if (images == null) return null;
        return images.stream().map(PostImage::getImageUrl).collect(Collectors.toList());
    }

    @Named("mapPlacesToIds")
    default List<Long> mapPlacesToIds(List<Place> places) {
        if (places == null) return null;
        return places.stream().map(Place::getId).collect(Collectors.toList());
    }
}
