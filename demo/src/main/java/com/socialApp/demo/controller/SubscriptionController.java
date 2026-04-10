package com.socialApp.demo.controller;

import com.socialApp.demo.dto.response.SubscriptionResponse;
import com.socialApp.demo.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/subscription")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @GetMapping("/getCurrentSubscription")
    public ResponseEntity<SubscriptionResponse> getCurrentSubscription() {
        SubscriptionResponse response = subscriptionService.getCurrentSubscription();
        return ResponseEntity.ok(response);  // ✅ returns 200 with null body for free tier
    }
}
