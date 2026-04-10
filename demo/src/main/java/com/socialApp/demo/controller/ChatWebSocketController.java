package com.socialApp.demo.controller;

import com.socialApp.demo.dto.request.ChatRequest;
import com.socialApp.demo.dto.response.ChatResponse;
import com.socialApp.demo.dto.response.JwtResponse;
import com.socialApp.demo.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/room/{roomId}/chat")
    public void sendMessage(
            @DestinationVariable Long roomId,
            ChatRequest chatRequest,
            Principal principal) {

        // ✅ Get userId from authenticated STOMP session, not from client payload
        JwtResponse user = (JwtResponse) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();

        ChatResponse savedMessage = chatService.saveMessage(roomId, chatRequest, user.userId());
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/chat", savedMessage);
    }
}