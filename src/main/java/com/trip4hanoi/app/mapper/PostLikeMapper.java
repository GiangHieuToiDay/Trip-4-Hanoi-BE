package com.trip4hanoi.app.mapper;

import com.trip4hanoi.app.dto.res.PostLikeResponse;
import com.trip4hanoi.app.entity.PostLike;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PostLikeMapper {

    @Mapping(source = "post.id", target = "postId")
    @Mapping(source = "user.id", target = "userId")
    PostLikeResponse toPostLikeResponse(PostLike postLike);
}
