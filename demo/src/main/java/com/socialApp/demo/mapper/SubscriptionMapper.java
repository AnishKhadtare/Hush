package com.socialApp.demo.mapper;

import com.socialApp.demo.dto.response.SubscriptionResponse;
import com.socialApp.demo.entity.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    @Mapping(source = "user.id",                      target = "userId")       // ✅ user not users
    @Mapping(source = "plan.id",                      target = "planId")
    @Mapping(source = "plan.planName",                target = "planName")
    @Mapping(source = "plan.maxMembersInRoom",         target = "maxMembersInRoom")
    @Mapping(source = "plan.maxRoomDurationInMinutes", target = "maxRoomDurationInMinutes")
    SubscriptionResponse toSubscriptionResponse(Subscription subscription);
}