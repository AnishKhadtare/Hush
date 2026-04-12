package com.socialApp.demo.service;

import com.socialApp.demo.controller.RoomMemberWebSocketController;
import com.socialApp.demo.dto.request.CreateRoomRequest;
import com.socialApp.demo.dto.response.CreateRoomResponse;
import com.socialApp.demo.dto.response.RoomWithInRadiusResponse;
import com.socialApp.demo.entity.*;
import com.socialApp.demo.enums.RoomMemberRole;
import com.socialApp.demo.enums.RoomMemberStatus;
import com.socialApp.demo.enums.RoomStatus;
import com.socialApp.demo.exception.ResourceNotFoundException;
import com.socialApp.demo.mapper.RoomMapper;
import com.socialApp.demo.repository.RoomMemberRepository;
import com.socialApp.demo.repository.RoomRepository;
import com.socialApp.demo.repository.UserRepository;
import com.socialApp.demo.security.AuthUtil;
import com.socialApp.demo.utils.CacheKeyUtils;
import com.socialApp.demo.utils.LocationUtil;
import com.socialApp.demo.utils.RoomMemberUtils;
import com.socialApp.demo.utils.RoomUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final UserRepository userRepository;
    private final RoomMapper roomMapper;
    private final AuthUtil authUtil;
    private final LocationUtil locationUtil;
    private final RoomMemberUtils roomMemberUtils;
    private final RoomUtils roomUtils;
    private final CacheKeyUtils cacheKeyUtils;
    private final RedisTemplate redisTemplate;
    private final RoomMemberWebSocketController roomMemberWebSocketController;
    private String roomMemberCountKey = "roomMemberCount";
    private String roomStatusKey = "roomStatus";

    @Transactional
    public ResponseEntity<CreateRoomResponse> createRoom(CreateRoomRequest createRoomRequest) {
        Long userId = authUtil.getCurrentLoggedInUserId();
        Room room  = roomMapper.toRoom(createRoomRequest);
        Users loggedInUser = getLoggedInUser(userId);

        room.setOwner(loggedInUser);
        room.setRoomStatus(RoomStatus.ACTIVE);
        room.setExpiresAt(new Date((long) (System.currentTimeMillis() + 1000*60*60*(createRoomRequest.roomDuration()))));

//      When user creates a room first the location of user is updated in userLocation
//      and that updated location is fetched and provided as centerLocation for that createdRoom.

        locationUtil.updateUserGeoLocation(userId, createRoomRequest.longitude(), createRoomRequest.latitude());
        room.setCenterLocation(loggedInUser.getUserLocation().getLocation());

//      Set the radius as per the subscription plan
//      room.setRadiusKM();

        Room createdRoom = roomRepository.save(room);

        CreateRoomResponse createRoomResponse = roomMapper.toRoomResponse(createdRoom);

        // Add the newly createdRoom in the cache. First the geoCache (long, lat, roomId) and second the roomDetailCache (room)
        // geoCache is for getting the list of roomIds which are particularly withing the specific range.
        // roomDetailCache is for getting the detail of specific room.

        redisTemplate.opsForGeo().add(
                cacheKeyUtils.roomGeoKey(),
                new org.springframework.data.geo.Point(createRoomRequest.longitude(), createRoomRequest.latitude()),
                createdRoom.getId().toString()
        );

        redisTemplate.opsForValue().set(
                cacheKeyUtils.roomDetailKey(createdRoom.getId()),
                createRoomResponse
        );

        // HashKey for maintaining the values that change dynamically like roomStatus and roomMemberCount
        redisTemplate.opsForHash().put(
                cacheKeyUtils.roomMetaDataKey(createdRoom.getId()),
                roomMemberCountKey,
                0
        );

        redisTemplate.opsForHash().put(
                cacheKeyUtils.roomMetaDataKey(createdRoom.getId()),
                roomStatusKey,
                "ACTIVE"
        );


//      Add the user that created room as owner in RoomMember.
        Long roomId = createdRoom.getId();

        roomMemberUtils.addUserToRoom(userId, roomId, RoomMemberRole.OWNER);

        roomRepository.joinRoom(roomId);

        return ResponseEntity.status(HttpStatus.CREATED).body(roomMapper.toRoomResponse(createdRoom));
    }

    public List<CreateRoomResponse> getRoomsWithinRadius(Double longitude, Double latitude) {

        Long userId = authUtil.getCurrentLoggedInUserId();

        if (longitude == null || latitude == null) {
            throw new ResourceNotFoundException("UserLocation co-ordinates", userId.toString());
        }

        locationUtil.updateUserGeoLocation(userId, longitude, latitude);

        Users user = getLoggedInUser(userId);
        Point userLocation = user.getUserLocation().getLocation();

        GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                redisTemplate.opsForGeo().search(
                        cacheKeyUtils.roomGeoKey(),
                        GeoReference.fromCoordinate(userLocation.getX(), userLocation.getY()),
                        new Distance(5, Metrics.KILOMETERS)
                );

        if (results == null) return List.of();

        List<CreateRoomResponse> rooms = new ArrayList<>();

        for (GeoResult<RedisGeoCommands.GeoLocation<String>> result : results) {

            Long roomId = Long.valueOf(result.getContent().getName());
            String roomDetailKey = cacheKeyUtils.roomDetailKey(roomId);
            String roomMetaKey = cacheKeyUtils.roomMetaDataKey(roomId);

            // 1️⃣ Try cache hit
            CreateRoomResponse cachedRoom =
                    (CreateRoomResponse) redisTemplate.opsForValue().get(roomDetailKey);

            Map<Object, Object> meta =
                    redisTemplate.opsForHash().entries(roomMetaKey);

            // 2️⃣ Cache Miss → Fetch From DB
            if (cachedRoom == null) {

                Room roomEntity = roomRepository.findById(roomId).orElse(null);
                if (roomEntity == null) continue;

                cachedRoom = roomMapper.toRoomResponse(roomEntity);

                // Save static data to cache
                redisTemplate.opsForValue().set(
                        roomDetailKey,
                        cachedRoom
                );

                // Save dynamic metadata
                redisTemplate.opsForHash().put(
                        roomMetaKey,
                        roomMemberCountKey,
                        roomEntity.getRoomMemberCount()
                );

                redisTemplate.opsForHash().put(
                        roomMetaKey,
                        roomStatusKey,
                        roomEntity.getRoomStatus().name()
                );

                meta = redisTemplate.opsForHash().entries(roomMetaKey);
            }

            // 3️⃣ Merge Static + Dynamic
            Integer memberCount =
                    meta.get(roomMemberCountKey) != null
                            ? Integer.valueOf(meta.get(roomMemberCountKey).toString())
                            : cachedRoom.roomMemberCount();

            String status =
                    meta.get(roomStatusKey) != null
                            ? meta.get(roomStatusKey).toString()
                            : cachedRoom.roomStatus().name();

            // 4️⃣ Create Final Response (Records are immutable)
            CreateRoomResponse finalResponse =
                    new CreateRoomResponse(
                            cachedRoom.id(),
                            cachedRoom.label(),
                            cachedRoom.description(),
                            cachedRoom.maxNumberOfMembers(),
                            RoomStatus.valueOf(status),
                            memberCount,
                            userId,
                            cachedRoom.tag(),
                            cachedRoom.expiresAt()
                    );

            rooms.add(finalResponse);
        }

        return rooms;
    }

//    public Room getRoomById(Long roomId){
//        return roomRepository.findById(roomId).orElseThrow(() -> new ResourceNotFoundException("Room", roomId.toString()));
//    }

    @PreAuthorize("""
            @roomMemberUtils.checkRoomJoiningEligibility(#roomId, #userId)
            """)
    @Transactional
    public ResponseEntity<Map<String, String>> joinRoom(Long roomId, Long userId){
        roomMemberUtils.addUserToRoom(userId, roomId, RoomMemberRole.MEMBER);
        roomRepository.joinRoom(roomId);
        Integer roomMemberCount = Integer.valueOf(redisTemplate.opsForHash().get(cacheKeyUtils.roomMetaDataKey(roomId), roomMemberCountKey).toString());
        roomMemberWebSocketController.publishRoomMemberCount(roomId, roomMemberCount);
        return ResponseEntity.ok(Map.of("message", "User joined successfully"));
    }

    //  the userId is of user to be banned
    // Pending : Purge the expired data after some time
    @PreAuthorize("""
                @roomMemberUtils.checkIsUserOwnerForRoom(#roomId)
            """)
    @Transactional
    public ResponseEntity<Map<String, String>> banUser(Long roomId, Long userId){
        Room room = roomUtils.getRoomFromRoomId(roomId);

        if(room.getRoomMemberCount() == 0)  return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message","Room member count can not be less than 0"));

        RoomMember roomMember = roomMemberUtils.getRoomMember(roomId, userId);

        if(roomMember.getRoomMemberRole() == RoomMemberRole.OWNER)  return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message","Owner can not ban itself"));

        roomMember.setRoomMemberStatus(RoomMemberStatus.BANNED);

        String metaDataKey = cacheKeyUtils.roomMetaDataKey(roomId);
        redisTemplate.opsForHash().increment(metaDataKey, roomMemberCountKey, -1);

        roomRepository.removeFromRoom(roomId);
        roomMemberRepository.save(roomMember);
        roomRepository.save(room);

        Integer updatedCount = Integer.valueOf(
                redisTemplate.opsForHash().get(metaDataKey, roomMemberCountKey).toString()
        );
        roomMemberWebSocketController.publishRoomMemberCount(roomId, updatedCount);

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "User banned from the room successfully"));

    }

    @Transactional
    public ResponseEntity<Map<String, String>> leaveRoom(Long roomId){
        Room room = roomUtils.getRoomFromRoomId(roomId);
        if(room.getRoomMemberCount() == 0)  return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Room member count can not be less than 0"));
        Long userId = authUtil.getCurrentLoggedInUserId();
        RoomMember roomMember = roomMemberUtils.getRoomMember(roomId, userId);
        RoomMemberRole roomMemberRole = roomMember.getRoomMemberRole();

        if(roomMemberRole == RoomMemberRole.OWNER){
//          Pending: Make the orphan room as expired after the timer exceeds the 2min. Then it would get auto deleted by cron job.
//          Also change the status of room in dynamic cache.
            room.setRoomStatus(RoomStatus.ORPHAN);
            room.setOwnerLeftDeletionAt(Date.from(Instant.now().plus(2, ChronoUnit.MINUTES)));
            roomRepository.save(room);

            String metaDataKey = cacheKeyUtils.roomMetaDataKey(roomId);
            redisTemplate.opsForHash().put(metaDataKey, roomStatusKey, "ORPHAN");
        }

        roomRepository.removeFromRoom(roomId);

        roomMember.setRoomMemberStatus(RoomMemberStatus.LEFT);

        roomMemberRepository.save(roomMember);

        String metaDataKey = cacheKeyUtils.roomMetaDataKey(roomId);
        redisTemplate.opsForHash().increment(metaDataKey, roomMemberCountKey, -1);

        Integer updatedCount = Integer.valueOf(
                redisTemplate.opsForHash().get(metaDataKey, roomMemberCountKey).toString()
        );
        roomMemberWebSocketController.publishRoomMemberCount(roomId, updatedCount);

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "User left the room"));
    }

    @PreAuthorize("""
                @roomMemberUtils.checkIsUserOwnerForRoom(#roomId)
            """)
    @Transactional
    public ResponseEntity<Map<String, String>> closeRoom(Long roomId){
        // Rooms are marked as EXPIRED and these expired room are later deleted via cron job. checck RoomUtil
        Room room = roomUtils.getRoomFromRoomId(roomId);
        room.setRoomStatus(RoomStatus.EXPIRED);
//      Delete all chats, roomMember, and room (KAFKA can be implemented as queue to delete?)
        roomRepository.save(room);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Room has been closed successfully."));
    }

    @PreAuthorize("""
                @roomMemberUtils.checkIsUserOwnerForRoom(#roomId)
            """)
    public ResponseEntity<Map<String, String>> toggleRoomLock(Long roomId){
        Room room = roomUtils.getRoomFromRoomId(roomId);
        room.setIsRoomLocked(!room.getIsRoomLocked());
        roomRepository.save(room);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Room lock status has been toggled successfully"));
    }

    public Users getLoggedInUser(Long userId){
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));
    }

    public ResponseEntity<CreateRoomResponse> getRoom(Long roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new ResourceNotFoundException("Room", roomId.toString()));
        return ResponseEntity.status(HttpStatus.OK).body(roomMapper.toRoomResponse(room));
    }
}

//After 2 min orphan changes to expire