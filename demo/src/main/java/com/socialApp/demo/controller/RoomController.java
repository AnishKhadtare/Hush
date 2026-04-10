package com.socialApp.demo.controller;

import com.socialApp.demo.dto.request.CreateRoomRequest;
import com.socialApp.demo.dto.response.CreateRoomResponse;
import com.socialApp.demo.dto.response.RoomWithInRadiusResponse;
import com.socialApp.demo.security.AuthUtil;
import com.socialApp.demo.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class RoomController {
    private final AuthUtil authUtil;
    private final RoomService roomService;

    @PostMapping("/create")
    public ResponseEntity<CreateRoomResponse> createRoom(@RequestBody CreateRoomRequest createRoomRequest){
        return roomService.createRoom(createRoomRequest);
    }

    @GetMapping("/getRoom/{roomId}")
    public ResponseEntity<CreateRoomResponse> getRoom(@PathVariable Long roomId){
        return roomService.getRoom(roomId);
    }

    @GetMapping("/getNearByRooms")
    public List<CreateRoomResponse> getNearbyRooms(@RequestParam Double longitude,@RequestParam Double latitude){
        return roomService.getRoomsWithinRadius(longitude, latitude);
    }

    @PostMapping("/join/{roomId}")
    public ResponseEntity<Map<String, String>> joinRoom(@PathVariable Long roomId){
        Long userId = authUtil.getCurrentLoggedInUserId();
        return roomService.joinRoom(roomId, userId);
    }

    @PostMapping("/banUser/{roomId}/{userId}")
    public ResponseEntity<Map<String, String>> banUser(@PathVariable Long roomId, @PathVariable Long userId){
        return roomService.banUser(roomId, userId);
    }

    @PostMapping("/leaveRoom/{roomId}")
    public ResponseEntity<Map<String, String>> leaveRoom(@PathVariable Long roomId){
        return roomService.leaveRoom(roomId);
    }

    @PostMapping("/closeRoom/{roomId}")
    public ResponseEntity<Map<String, String>> closeRoom(@PathVariable Long roomId){
        return roomService.closeRoom(roomId);
    }

    @PostMapping("/toggleRoomLock/{roomId}")
    public ResponseEntity<Map<String, String>> toggleRoomLock(@PathVariable Long roomId){
        return roomService.toggleRoomLock(roomId);
    }
}
