package com.socialApp.demo.dto.response;

import com.socialApp.demo.enums.SubscriptionStatus;
import java.time.Instant;

public record SubscriptionResponse(
        Long id,
        Long userId,
        Long planId,
        String planName,                  // ✅
        Integer maxMembersInRoom,         // ✅
        Integer maxRoomDurationInMinutes, // ✅
        SubscriptionStatus subscriptionStatus,
        String stripeSubscriptionId,
        Instant startedAt,
        Instant expiresAt
) {}