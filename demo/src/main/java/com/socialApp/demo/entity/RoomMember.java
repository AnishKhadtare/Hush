package com.socialApp.demo.entity;

import com.socialApp.demo.enums.RoomMemberRole;
import com.socialApp.demo.enums.RoomMemberStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoomMember {
    @EmbeddedId
    RoomMemberId roomMemberId;

    @Enumerated(EnumType.STRING)
    RoomMemberStatus roomMemberStatus;

    @Enumerated(EnumType.STRING)
    RoomMemberRole roomMemberRole;

    @MapsId("roomId")
    @JoinColumn()
    @ManyToOne()
    Room room;
}
