package com.socialApp.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true, nullable = false)
    String planName;

    @Column(nullable = false)
    Double price;

    @Column(unique = true, nullable = false)
    String priceId;

    @Column(nullable = false)
    Integer maxMembersInRoom;

    @Column(nullable = false)
    Integer maxRoomDurationInMinutes;

    // Currently ignoring this one.
    @Column(nullable = false)
    Integer maxRadiusKM;
}
