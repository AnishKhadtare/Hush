package com.socialApp.demo.dto.response;

import com.socialApp.demo.entity.Users;
import com.socialApp.demo.enums.RoomStatus;

import java.util.Date;

public record RoomWithInRadiusResponse(
        String label,
        String description,
        Integer maxNumberOfMembers,
        String tag,
        Integer roomMemberCount,
        String roomStatus,
        Users owner,
        Date expiresAt
) {
}
