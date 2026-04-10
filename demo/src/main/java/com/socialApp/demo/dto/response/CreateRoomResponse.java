package com.socialApp.demo.dto.response;
import com.socialApp.demo.entity.Users;
import com.socialApp.demo.enums.RoomStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

public record CreateRoomResponse(
        Long id,
        String label,
        String description,
        Integer maxNumberOfMembers,
        RoomStatus roomStatus,
        Integer roomMemberCount,
        Long ownerId,
        String tag,
        Date expiresAt
) {
}
