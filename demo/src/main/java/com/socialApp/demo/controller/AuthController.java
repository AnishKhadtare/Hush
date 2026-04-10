package com.socialApp.demo.controller;

import com.socialApp.demo.dto.request.UserSignInRequest;
import com.socialApp.demo.dto.request.UserSignUpRequest;
import com.socialApp.demo.dto.response.UserSignInResponse;
import com.socialApp.demo.dto.response.UserSignUpResponse;
import com.socialApp.demo.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/auth/signUp")
    public UserSignUpResponse signUp(@RequestBody UserSignUpRequest userSignUpRequest){
        return authService.signUp(userSignUpRequest);
    }

    @PostMapping("/auth/login")
    public UserSignInResponse login(@RequestBody UserSignInRequest userSignInRequest){
        return authService.login(userSignInRequest);
    }
}
