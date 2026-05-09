package com.trip4hanoi.app.mapper;


import com.trip4hanoi.app.dto.res.ChatRoomResponse;
import com.trip4hanoi.app.entity.ChatRoom;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring" , uses = {ChatMessageMapper.class})
public interface ChatRoomMapper {


    @Mapping(target = "userId" ,source = "user.id")
    @Mapping(target = "userName", source = "user.username")
    @Mapping(target = "staffId", source = "staff.id")
    @Mapping(target = "staffName", source = "staff.username")
    @Mapping(target = "lastMessage", ignore = true)// set thủ công trong Service
    ChatRoomResponse tcChatRoomResponse(ChatRoom chatRoom);




}
