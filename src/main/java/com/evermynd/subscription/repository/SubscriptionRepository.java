package com.evermynd.subscription.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.evermynd.subscription.entity.Subscription;
import com.evermynd.subscription.enums.SubscriptionStatus;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    Optional<Subscription> findByUserIdAndStatus(UUID userId, SubscriptionStatus status);

    List<Subscription> findByUserIdOrderByStartedAtDesc(UUID userId);

    List<Subscription> findByStatusAndExpiresAtBefore(SubscriptionStatus status, LocalDateTime dateTime);
}