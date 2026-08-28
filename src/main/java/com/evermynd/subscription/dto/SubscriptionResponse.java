package com.evermynd.subscription.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.evermynd.subscription.entity.Subscription;
import com.evermynd.subscription.enums.Plan;
import com.evermynd.subscription.enums.SubscriptionStatus;

public record SubscriptionResponse(
        UUID id,
        Plan plan,
        SubscriptionStatus status,
        LocalDateTime startedAt,
        LocalDateTime expiresAt,
        String paymentId
) {
    public static SubscriptionResponse from(Subscription subscription) {
        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getPlan(),
                subscription.getStatus(),
                subscription.getStartedAt(),
                subscription.getExpiresAt(),
                subscription.getPaymentId()
        );
    }
}