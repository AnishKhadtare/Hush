package com.socialApp.demo.dto.response;

import com.socialApp.demo.enums.MessageType;

import java.time.LocalDateTime;

public record ChatResponse (
    Long id,
    Long roomId,
    Long senderId,
    String content,
    MessageType messageType,
    LocalDateTime createdAt
){}
