package com.socialApp.demo.utils;

import com.socialApp.demo.entity.Room;
import com.socialApp.demo.entity.RoomMember;
import com.socialApp.demo.entity.RoomMemberId;
import com.socialApp.demo.entity.Users;
import com.socialApp.demo.enums.RoomMemberRole;
import com.socialApp.demo.enums.RoomMemberStatus;
import com.socialApp.demo.enums.RoomStatus;
import com.socialApp.demo.exception.ResourceNotFoundException;
import com.socialApp.demo.repository.RoomMemberRepository;
import com.socialApp.demo.repository.RoomRepository;
import com.socialApp.demo.repository.UserRepository;
import com.socialApp.demo.security.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;


import java.util.Date;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RoomMemberUtils {
    private final RoomMemberRepository roomMemberRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomUtils roomUtils;
    private final AuthUtil authUtil;
    private final RedisTemplate redisTemplate;
    private final CacheKeyUtils cacheKeyUtils;
    private String roomMemberCountKey = "roomMemberCount";
    private String roomStatusKey = "roomStatus";



    public void addUserToRoom(Long userId, Long roomId, RoomMemberRole roomMemberRole){
        RoomMemberId roomMemberId = new RoomMemberId(roomId, userId);

        Room room = roomUtils.getRoomFromRoomId(roomId);
        boolean isRoomMemberFound = roomMemberRepository.existsById(roomMemberId);

        if(isRoomMemberFound){
            RoomMember member = this.getRoomMember(roomId, userId);
            if(room.getRoomStatus() == RoomStatus.ORPHAN && member.getRoomMemberRole() == RoomMemberRole.OWNER
                    && room.getOwnerLeftDeletionAt().before(new Date())){
                room.setRoomStatus(RoomStatus.EXPIRED);
                room.setExpiresAt(new Date());
                roomRepository.save(room);
                return;
            }
        }

        roomMemberRepository.findById(roomMemberId).ifPresent(roomMember -> {
            roomMember.setRoomMemberStatus(RoomMemberStatus.ACTIVE);
            room.setRoomStatus(RoomStatus.ACTIVE);
            room.setOwnerLeftDeletionAt(null);
            roomMemberRepository.save(roomMember);
            roomRepository.save(room);
        });

        if(!isRoomMemberFound){
            String metaDataKey = cacheKeyUtils.roomMetaDataKey(roomId);
            redisTemplate.opsForHash().increment(metaDataKey, roomMemberCountKey, 1);

            RoomMember newRoomMember = new RoomMember();

            newRoomMember.setRoomMemberId(roomMemberId);
            newRoomMember.setRoomMemberStatus(RoomMemberStatus.ACTIVE);
            newRoomMember.setRoomMemberRole(roomMemberRole);

            newRoomMember.setRoom(room);

            roomMemberRepository.save(newRoomMember);


        }

    }

    public boolean checkRoomJoiningEligibility(Long roomId, Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId.toString()));

        // Check if room is expired or full
        if (room.getRoomStatus() == RoomStatus.EXPIRED || room.getRoomMemberCount() >= room.getMaxNumberOfMembers() || room.getIsRoomLocked()) {
            return false;
        }

        // Check if user is banned (only if they have an existing record)
        RoomMemberId roomMemberId = new RoomMemberId(userId, roomId);
        Optional<RoomMember> existingMember = roomMemberRepository.findById(roomMemberId);
        if (existingMember.isPresent() && existingMember.get().getRoomMemberStatus() == RoomMemberStatus.BANNED) {
            return false;
        }

        // Check radius
        return isRoomWithinRadius(room, user);
    }

    public boolean checkIsUserOwnerForRoom(Long roomId){
        Long userId = authUtil.getCurrentLoggedInUserId();
        System.out.println("adbjdlllvbvadladbvlablaba;abnadnl;ab1     "+userId);
        RoomMemberId roomMemberId = new RoomMemberId(roomId, userId);

        boolean roomMemberExists = roomMemberRepository.existsById(roomMemberId);
        System.out.println("adbjdlllvbvadladbvlablaba;abnadnl;ab2     "+roomMemberExists);
        if(!roomMemberExists){
            return false;
        }
        RoomMember existingRoomMember = roomMemberRepository.findById(roomMemberId).orElseThrow(() -> new ResourceNotFoundException("RoomMember", roomMemberId.toString()));
        System.out.println("adbjdlllvbvadladbvlablaba;abnadnl;ab3     "+existingRoomMember);
        if(existingRoomMember.getRoomMemberRole() != RoomMemberRole.OWNER){
            return false;
        }
        System.out.println("adbjdlllvbvadladbvlablaba;abnadnl;ab     TRUEPASSED"+userId);
        return true;
    }

    public RoomMember getRoomMember(Long roomId, Long userId){
        RoomMemberId roomMemberId = new RoomMemberId(roomId, userId);
        return roomMemberRepository.findById(roomMemberId).orElseThrow(() -> new ResourceNotFoundException("RoomMember", roomMemberId.toString()));
    }

    public boolean isRoomWithinRadius(Room room, Users user){
        return roomRepository.isRoomWithinRadius(room.getId(), user.getUserLocation().getLocation().getX(), user.getUserLocation().getLocation().getY());
    }
}
