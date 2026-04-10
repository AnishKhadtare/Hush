package com.socialApp.demo.service;

import com.socialApp.demo.dto.request.ChatRequest;
import com.socialApp.demo.dto.response.ChatResponse;
import com.socialApp.demo.entity.Message;
import com.socialApp.demo.entity.Room;
import com.socialApp.demo.entity.Users;
import com.socialApp.demo.enums.MessageType;
import com.socialApp.demo.exception.ResourceNotFoundException;
import com.socialApp.demo.mapper.MessageMapper;
import com.socialApp.demo.repository.MessageRepository;
import com.socialApp.demo.repository.UserRepository;
import com.socialApp.demo.utils.RoomUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final RoomUtils roomUtils;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;

    public ChatResponse saveMessage(Long roomId, ChatRequest chatRequest, Long userId){
        Room room =  roomUtils.getRoomFromRoomId(roomId);
        Users sender = userRepository.findById(userId)   // ✅ from Principal, never null
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        Message message = new Message();
        message.setRoom(room);
        message.setSender(sender);
        message.setPayload(chatRequest.content());
        message.setMessageType(MessageType.TEXT);

        Message createdMessage = messageRepository.save(message);
        return messageMapper.toChatResponse(createdMessage);
    }

    public Page<ChatResponse> getRoomMessage(Long roomId, Pageable pageable){
        return messageRepository.findByRoomIdOrderByCreatedAtDesc(roomId, pageable)
                .map(messageMapper::toChatResponse);
    }
}
