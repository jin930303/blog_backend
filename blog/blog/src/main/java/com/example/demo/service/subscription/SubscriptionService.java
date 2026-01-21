package com.example.demo.service.subscription;

public interface SubscriptionService {
    boolean toggleSubscription(Long currentMemberId, Long targetId);

    boolean checkSubscriptionStatus(Long currentMemberId, Long targetId);
}
