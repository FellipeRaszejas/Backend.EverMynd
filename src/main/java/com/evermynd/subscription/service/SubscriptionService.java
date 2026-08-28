package com.evermynd.subscription.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.evermynd.security.CurrentUserProvider;
import com.evermynd.subscription.dto.CreateSubscriptionRequest;
import com.evermynd.subscription.dto.SubscriptionResponse;
import com.evermynd.subscription.entity.Subscription;
import com.evermynd.subscription.enums.SubscriptionStatus;
import com.evermynd.subscription.exception.ActiveSubscriptionAlreadyExistsException;
import com.evermynd.subscription.exception.PaymentFailedException;
import com.evermynd.subscription.exception.SubscriptionNotFoundException;
import com.evermynd.subscription.gateway.PaymentGateway;
import com.evermynd.subscription.repository.SubscriptionRepository;
import com.evermynd.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final CurrentUserProvider currentUserProvider;
    private final PaymentGateway paymentGateway;

    public SubscriptionResponse create(CreateSubscriptionRequest request) {
        User currentUser = currentUserProvider.getCurrentUser();

        subscriptionRepository.findByUserIdAndStatus(currentUser.getId(), SubscriptionStatus.ACTIVE)
                .ifPresent(existing -> {
                    throw new ActiveSubscriptionAlreadyExistsException("Usuário já possui uma assinatura ativa");
                });

        UUID attemptReference = UUID.randomUUID();
        PaymentGateway.ChargeRequest chargeRequest = new PaymentGateway.ChargeRequest(
                request.cardToken(),
                request.paymentMethodId(),
                currentUser.getEmail(),
                request.plan(),
                attemptReference.toString()
        );

        PaymentGateway.PaymentResult payment = paymentGateway.charge(chargeRequest);
        if (!payment.approved()) {
            throw new PaymentFailedException(
                    payment.statusDetail() != null ? payment.statusDetail() : "Pagamento recusado");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = switch (request.plan()) {
            case MONTHLY -> now.plusMonths(1);
            case ANNUAL -> now.plusYears(1);
        };

        Subscription subscription = Subscription.builder()
                .user(currentUser)
                .plan(request.plan())
                .status(SubscriptionStatus.ACTIVE)
                .startedAt(now)
                .expiresAt(expiresAt)
                .paymentId(payment.paymentId())
                .build();

        return SubscriptionResponse.from(subscriptionRepository.save(subscription));
    }

    public List<SubscriptionResponse> listMySubscriptions() {
        UUID userId = currentUserProvider.getCurrentUserId();
        return subscriptionRepository.findByUserIdOrderByStartedAtDesc(userId).stream()
                .map(SubscriptionResponse::from)
                .toList();
    }

    public SubscriptionResponse getActiveSubscription() {
        UUID userId = currentUserProvider.getCurrentUserId();
        Subscription subscription = subscriptionRepository
                .findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new SubscriptionNotFoundException("Nenhuma assinatura ativa encontrada"));
        return SubscriptionResponse.from(subscription);
    }

    public SubscriptionResponse cancel(UUID subscriptionId) {
        UUID userId = currentUserProvider.getCurrentUserId();
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new SubscriptionNotFoundException("Assinatura não encontrada: " + subscriptionId));

        if (!subscription.getUser().getId().equals(userId)) {
            throw new SubscriptionNotFoundException("Assinatura não encontrada: " + subscriptionId);
        }

        subscription.setStatus(SubscriptionStatus.CANCELED);
        return SubscriptionResponse.from(subscriptionRepository.save(subscription));
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void expireOutdatedSubscriptions() {
        List<Subscription> expired = subscriptionRepository
                .findByStatusAndExpiresAtBefore(SubscriptionStatus.ACTIVE, LocalDateTime.now());
        expired.forEach(s -> s.setStatus(SubscriptionStatus.EXPIRED));
        subscriptionRepository.saveAll(expired);
    }
}