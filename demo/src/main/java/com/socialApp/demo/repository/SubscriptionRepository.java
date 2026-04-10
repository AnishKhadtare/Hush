package com.socialApp.demo.repository;

import com.socialApp.demo.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Subscription findByStripeSubscriptionId(String id);
    Optional<Subscription> findByUser_Id(Long userId);
}

