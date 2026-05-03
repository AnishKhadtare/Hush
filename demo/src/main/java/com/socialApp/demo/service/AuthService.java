package com.socialApp.demo.service;

import com.socialApp.demo.dto.request.UserSignInRequest;
import com.socialApp.demo.dto.request.UserSignUpRequest;
import com.socialApp.demo.dto.response.UserSignInResponse;
import com.socialApp.demo.dto.response.UserSignUpResponse;
import com.socialApp.demo.entity.Users;
import com.socialApp.demo.exception.BadRequestException;
import com.socialApp.demo.mapper.UserMapper;
import com.socialApp.demo.repository.UserLocationRepository;
import com.socialApp.demo.repository.UserRepository;
import com.socialApp.demo.security.AuthUtil;
import com.socialApp.demo.utils.LocationUtil;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    @Lazy
    private final AuthenticationManager authenticationManager;
    private final AuthUtil authUtil;
    private final LocationUtil locationUtil;
    private final Logger log = LoggerFactory.getLogger(AuthService.class);

    public UserSignUpResponse signUp(UserSignUpRequest userSignUpRequest) {
        boolean userExists = userRepository.findByUsername(userSignUpRequest.username()).isPresent();
        if(userExists){
            log.warn("User already exists sign up operation failed.");
            throw new BadRequestException("User already exists with username: " + userSignUpRequest.username());
        }
        Users user = userMapper.toUserSignUp(userSignUpRequest);

        user.setPassword(passwordEncoder.encode(userSignUpRequest.password()));

        userRepository.save(user);
        log.debug("User saved successfully with userId : {}", user.getId());
        log.debug("Update the location of user as current location of signup");
        locationUtil.configureUserGeoLocation(user, userSignUpRequest.longitude(), userSignUpRequest.latitude());
        log.debug("Updated the location of user as current location of signup");

        log.info("User successfully signed up");
        return userMapper.toUserSignUpResponse(user);
    }

    public UserSignInResponse login(UserSignInRequest userSignInRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userSignInRequest.username(), userSignInRequest.password())
        );
        log.debug("User credentials entered are same as that of user credentials in db");
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        log.debug("Generating token for the user trying to login");
        Users user = customUserDetails.getUser();
        log.debug("Token generation successfull");
        String token = authUtil.generateToken(user);
        return new UserSignInResponse(token);
    }

}
