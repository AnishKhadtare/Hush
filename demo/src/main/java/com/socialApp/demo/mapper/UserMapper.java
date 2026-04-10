package com.socialApp.demo.mapper;

import com.socialApp.demo.dto.request.UserSignUpRequest;
import com.socialApp.demo.dto.response.UserSignUpResponse;
import com.socialApp.demo.entity.Users;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    Users toUserSignUp(UserSignUpRequest userSignUpRequest);
    UserSignUpResponse toUserSignUpResponse(Users users);
}
