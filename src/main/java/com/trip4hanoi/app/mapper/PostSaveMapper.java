package com.trip4hanoi.app.mapper;

import com.trip4hanoi.app.dto.res.PostSaveResponse;
import com.trip4hanoi.app.entity.PostSave;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PostSaveMapper {

    @Mapping(source = "post.id", target = "postId")
    @Mapping(source = "user.id", target = "userId")
    PostSaveResponse toPostSaveResponse(PostSave postSave);
}
