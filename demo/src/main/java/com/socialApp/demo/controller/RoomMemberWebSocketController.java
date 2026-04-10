package com.socialApp.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class RoomMemberWebSocketController {
    private final SimpMessagingTemplate messagingTemplate;

    public void publishRoomMemberCount(Long roomId, int count){
        messagingTemplate.convertAndSend("/topic/room/"+roomId+"/memberCount", count);
    }
}
