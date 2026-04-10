package com.socialApp.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.socialApp.demo.enums.RoomStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(length = 50, nullable = false)
    String label;

    @Column(nullable = false)
    String description;

    Integer roomMemberCount = 0;

    Integer maxNumberOfMembers;

    @Column(unique = true, nullable = true)
    Long roomCode;

    String tag;

    @Enumerated(EnumType.STRING)
    RoomStatus roomStatus;

//    @Embedded
//    GeoLocation centerLocation;


    @Column(columnDefinition = "geometry(Point,4326)")
//    @Type(type = "org.hibernate.spatial.GeometryType")
    Point centerLocation;

    @Column(name = "radiuskm")
    Integer radiusKM=5;

    @ManyToOne
    @JoinColumn(name="owner_id")
    @JsonIgnore
    Users owner;

    @OneToMany(mappedBy = "room", cascade = CascadeType.REMOVE)
    @JsonIgnore
    List<RoomMember> roomMembers = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.REMOVE)
    @JsonIgnore
    List<Message> chatMessages = new ArrayList<>();

    Boolean isRoomLocked = false;

    @CreationTimestamp
    Date createdAt;

    Date expiresAt;

    Date ownerLeftDeletionAt;
}
