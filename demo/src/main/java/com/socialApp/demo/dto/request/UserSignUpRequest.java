package com.socialApp.demo.dto.request;

public record UserSignUpRequest(
        String username,
        String password,
        Double longitude,
        Double latitude
) {
}
