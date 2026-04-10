package com.socialApp.demo.repository;

import com.socialApp.demo.entity.UserLocation;
import com.socialApp.demo.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserLocationRepository extends JpaRepository<UserLocation, Long> {
}
