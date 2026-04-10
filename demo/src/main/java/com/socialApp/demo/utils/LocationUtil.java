package com.socialApp.demo.utils;

import com.socialApp.demo.entity.UserLocation;
import com.socialApp.demo.entity.Users;
import com.socialApp.demo.exception.ResourceNotFoundException;
import com.socialApp.demo.repository.UserLocationRepository;
import com.socialApp.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocationUtil {
    private final UserLocationRepository userLocationRepository;
    private final UserRepository userRepository;

    public void configureUserGeoLocation(Users user, Double longitude, Double latitude){
        UserLocation userLocation = new UserLocation();
        userLocation.setUser(user);

        Point point = createGeometricPoint(longitude, latitude);

        userLocation.setLocation(point);
        userLocationRepository.save(userLocation);

        user.setUserLocation(userLocation);

        userRepository.save(user);
    }

    public void updateUserGeoLocation(Long userId, Double longitude, Double latitude){
        UserLocation userLocation = userLocationRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserLocation", userId.toString()));

        Point updatedLocationPoint = createGeometricPoint(longitude, latitude);
        updatedLocationPoint.setSRID(4326);
        userLocation.setLocation(updatedLocationPoint);

        userLocationRepository.save(userLocation);
    }

    public Point createGeometricPoint(Double longitude, Double latitude){
        GeometryFactory geometryFactory = new GeometryFactory();
        return geometryFactory.createPoint(
                new Coordinate(longitude, latitude)
        );
    }
}
