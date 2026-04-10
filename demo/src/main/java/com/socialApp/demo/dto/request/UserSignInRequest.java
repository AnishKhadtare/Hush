package com.socialApp.demo.dto.request;

public record UserSignInRequest(
        String username,
        String password
) {
}
