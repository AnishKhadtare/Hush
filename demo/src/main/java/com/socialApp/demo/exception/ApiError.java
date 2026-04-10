package com.socialApp.demo.exception;

import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

public record ApiError(
        HttpStatus httpStatus,
        String message,
        Instant timestamp,
        List<ApiFieldError> errors
) {
    public ApiError(HttpStatus httpStatus, String message){
        this(httpStatus, message, Instant.now(), null);
    }

    public ApiError(HttpStatus httpStatus, String message, List<ApiFieldError> errors){
        this(httpStatus, message, Instant.now(), errors);
    }
}

record ApiFieldError(String field, String message){}