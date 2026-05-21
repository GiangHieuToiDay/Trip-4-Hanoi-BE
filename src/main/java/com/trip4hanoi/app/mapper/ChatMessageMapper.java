package com.trip4hanoi.app.mapper;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.trip4hanoi.app.dto.res.ChatMessageResponse;
import com.trip4hanoi.app.entity.ChatMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper {

    // Khởi tạo ObjectMapper để dùng trong method default
    ObjectMapper objectMapper = new ObjectMapper();





    @Mapping(target = "senderId", source = "sender.id")
    @Mapping(target = "senderName" , source = "sender.actualUsername")
    @Mapping(target = "senderAvatar", source = "sender.avatar")
    @Mapping(target = "mediaUrls", source = "mediaUrls", qualifiedByName = "mapMediaUrls")
    ChatMessageResponse toChatMessageResponse(ChatMessage chatMessage);


    @Named("mapMediaUrls")
    default List<String> mapMediaUrls(String source) {
        if(source ==null || source.isEmpty()){
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(source,new TypeReference<List<String>>() {});
        }
        catch (Exception e){
            return new ArrayList<>();
        }

    }

}

