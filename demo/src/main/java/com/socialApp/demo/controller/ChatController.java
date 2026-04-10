package com.socialApp.demo.controller;

import com.socialApp.demo.dto.request.ChatRequest;
import com.socialApp.demo.dto.response.ChatResponse;
import com.socialApp.demo.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class ChatController {
    private final ChatService chatService;

    @GetMapping("/{roomId}/messages")
    public Page<ChatResponse> getMessage(@PathVariable Long roomId,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "50") int size){
        return chatService.getRoomMessage(roomId, PageRequest.of(page, size));
    }
}
