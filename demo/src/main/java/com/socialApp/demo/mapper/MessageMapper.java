package com.socialApp.demo.mapper;

import com.socialApp.demo.dto.response.ChatResponse;
import com.socialApp.demo.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    @Mapping(source = "room.id", target = "roomId")
    @Mapping(source = "sender.id", target = "senderId")
    @Mapping(source = "payload", target = "content")
    ChatResponse toChatResponse(Message message);
}