package com.socialApp.demo.dto.request;

public record CreateRoomRequest(
        String label,
        String description,
        Integer maxNumberOfMembers,
        Long roomCode,
        String tag,
        /*Extras parameter not in entity from frontend*/
        Double roomDuration,
        Double longitude,
        Double latitude
) {
}
