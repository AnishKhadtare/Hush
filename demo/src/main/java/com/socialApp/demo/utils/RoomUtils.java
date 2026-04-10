package com.socialApp.demo.utils;

import com.socialApp.demo.entity.Room;
import com.socialApp.demo.enums.RoomStatus;
import com.socialApp.demo.exception.ResourceNotFoundException;
import com.socialApp.demo.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RoomUtils {
    private final RoomRepository roomRepository;
    private final RedisTemplate redisTemplate;
    private final CacheKeyUtils cacheKeyUtils;

    public Room getRoomFromRoomId(Long roomId){
        return roomRepository.findById(roomId).orElseThrow(() -> new ResourceNotFoundException("Room", roomId.toString()));
    }

    @Scheduled(fixedDelay = 10_000 * 6)  // every minute
    @Transactional
    public void markExpiredRooms() {
        List<Room> rooms = roomRepository.findByRoomStatusAndExpiresAtBefore(
                RoomStatus.ACTIVE,
                new Date()
        );
        rooms.forEach(room -> room.setRoomStatus(RoomStatus.EXPIRED));
        roomRepository.saveAll(rooms);
    }

    @Scheduled(fixedDelay = 10_000 * 6)
    @Transactional
    public void deleteExpiredRooms() {
        List<Room> rooms = roomRepository.findByRoomStatusAndExpiresAtBefore(
                RoomStatus.EXPIRED,
                new Date()
        );

        rooms.forEach(room -> {
            // ✅ remove from geo cache
            redisTemplate.opsForGeo().remove(
                    cacheKeyUtils.roomGeoKey(),
                    room.getId().toString()
            );
            // ✅ remove room detail cache
            redisTemplate.delete(cacheKeyUtils.roomDetailKey(room.getId()));
            // ✅ remove metadata cache
            redisTemplate.delete(cacheKeyUtils.roomMetaDataKey(room.getId()));
        });

        roomRepository.deleteAll(rooms);
    }

    @Scheduled(fixedDelay = 10_000 * 6)
    @Transactional
    public void expireOrphanRooms() {
        List<Room> rooms = roomRepository.findByRoomStatusAndOwnerLeftDeletionAtBefore(
                RoomStatus.ORPHAN,
                new Date()
        );
        rooms.forEach(room -> room.setRoomStatus(RoomStatus.EXPIRED));
        roomRepository.saveAll(rooms);
    }
}
