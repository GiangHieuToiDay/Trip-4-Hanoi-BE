package com.trip4hanoi.app.mapper;

import com.trip4hanoi.app.dto.req.CommentRequest;
import com.trip4hanoi.app.dto.res.CommentResponse;
import com.trip4hanoi.app.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(source = "post.id", target = "postId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.name", target = "userName")
    CommentResponse toCommentResponse(Comment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "post", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Comment toComment(CommentRequest request);
}
