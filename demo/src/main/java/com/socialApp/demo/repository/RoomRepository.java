package com.socialApp.demo.repository;

import com.socialApp.demo.entity.Room;
import com.socialApp.demo.enums.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

/*    @Query("""
            SELECT * from Room r WHERE
            r.roomStatus <> 'EXPIRED'
            AND ST_DWithin(
                r.centerLocation,
                ST_MakePoint(:lon, :lat)::geography,
                r.radiusKM
            );
            """)
    List<Room> findRoomsWithinRadius(@Param("lat") Double lat, @Param("lon") Double lon);*/

    @Query(value = "SELECT * FROM room r " +
            "WHERE r.room_status <> 'EXPIRED' " +
            "AND ST_DistanceSphere(r.center_location, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)) <= r.radiuskm * 1000",
            nativeQuery = true)
    List<Room> findRoomsWithinRadius(@Param("lon") Double lon,
                                     @Param("lat") Double lat);

    @Query("""
           UPDATE Room r SET r.roomMemberCount = r.roomMemberCount+1 WHERE r.id = :roomId
           """)
    @Modifying
    void joinRoom(Long roomId);


    @Query("""
           UPDATE Room r SET r.roomMemberCount = r.roomMemberCount-1 WHERE r.id = :roomId
           """)
    @Modifying
    void removeFromRoom(Long roomId);


    /*@Query("""
           SELECT EXISTS (
                SELECT 1 FROM Room r
                    WHERE r.id = :roomId
                    AND ST_DWithin(
                        r.centerLocation,
                        ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography,
                        r.radiusKM
                    )
           )
           """)
    boolean isRoomWithinRadius(@Param("roomId") Long roomId, @Param("lon") Double longitude, @Param("lat") Double latitude);*/


    @Query(value = "SELECT EXISTS ( " +
            "SELECT 1 FROM room r " +
            "WHERE r.id = :roomId " +
            "AND ST_DistanceSphere( " +
            "    r.center_location, " +
            "    ST_SetSRID(ST_MakePoint(:lon, :lat), 4326) " +
            ") <= r.radiuskm * 1000 " +
            ")",
            nativeQuery = true)
    boolean isRoomWithinRadius(@Param("roomId") Long roomId,
                               @Param("lon") Double lon,
                               @Param("lat") Double lat);

    List<Room> findByRoomStatusAndOwnerLeftDeletionAtBefore(RoomStatus roomStatus, Date date);

    List<Room> findByRoomStatusAndExpiresAtBefore(RoomStatus roomStatus, Date date);
}
