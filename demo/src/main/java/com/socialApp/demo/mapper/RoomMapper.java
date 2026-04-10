package com.socialApp.demo.mapper;

import com.socialApp.demo.dto.request.CreateRoomRequest;
import com.socialApp.demo.dto.response.CreateRoomResponse;
import com.socialApp.demo.dto.response.RoomWithInRadiusResponse;
import com.socialApp.demo.entity.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoomMapper {
    Room toRoom(CreateRoomRequest createRoomRequest);
    @Mapping(source = "owner.id", target = "ownerId")    CreateRoomResponse toRoomResponse(Room room);
    List<RoomWithInRadiusResponse> toRoomWithInRadiusResponseList (List<Room> rooms);
    Room toRoomFromRoomWithinRadius (RoomWithInRadiusResponse roomWithInRadiusResponse);
}
