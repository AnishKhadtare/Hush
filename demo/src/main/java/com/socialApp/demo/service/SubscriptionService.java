package com.socialApp.demo.service;

import com.socialApp.demo.dto.response.SubscriptionResponse;
import com.socialApp.demo.entity.Subscription;
import com.socialApp.demo.exception.ResourceNotFoundException;
import com.socialApp.demo.mapper.SubscriptionMapper;
import com.socialApp.demo.repository.SubscriptionRepository;
import com.socialApp.demo.security.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final AuthUtil authUtil;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;

    public SubscriptionResponse getCurrentSubscription() {
        Long userId = authUtil.getCurrentLoggedInUserId();
        return subscriptionRepository.findByUser_Id(userId)
                .map(subscriptionMapper::toSubscriptionResponse)
                .orElse(null);  // ✅ return null instead of throwing — means free tier
    }
}
