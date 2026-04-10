package com.socialApp.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UpdateTimestamp;
import org.locationtech.jts.geom.Point;

import java.util.Date;

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserLocation {
    @Id
    Long userId;

    @OneToOne()
    @MapsId
    @JsonIgnore
    @JoinColumn(name = "user_id")
    Users user;

//    @Embedded
//    GeoLocation location;

    Point location;

    @UpdateTimestamp
    Date updatedAt;
}