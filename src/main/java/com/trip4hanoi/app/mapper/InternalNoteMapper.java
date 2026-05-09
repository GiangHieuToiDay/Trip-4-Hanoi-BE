package com.trip4hanoi.app.mapper;

import com.trip4hanoi.app.dto.res.InternalNoteResponse;
import com.trip4hanoi.app.entity.InternalNote;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InternalNoteMapper {

    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorName", source = "author.username")
    InternalNoteResponse toInternalNoteResponse(InternalNote internalNote);
}
