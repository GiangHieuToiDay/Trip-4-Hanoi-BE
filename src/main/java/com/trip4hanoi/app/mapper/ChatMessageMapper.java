package com.trip4hanoi.app.mapper;


import com.trip4hanoi.app.dto.res.ChatMessageResponse;
import com.trip4hanoi.app.entity.ChatMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper {
    @Mapping(target = "senderId", source = "sender.id")
    @Mapping(target = "senderName" , source = "sender.actualUsername")
    @Mapping(target = "senderAvatar", source = "sender.avatar")
    ChatMessageResponse toChatMessageResponse(ChatMessage chatMessage);


}

