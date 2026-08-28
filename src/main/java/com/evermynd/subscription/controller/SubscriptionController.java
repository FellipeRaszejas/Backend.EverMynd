package com.evermynd.subscription.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.evermynd.subscription.dto.CreateSubscriptionRequest;
import com.evermynd.subscription.dto.SubscriptionResponse;
import com.evermynd.subscription.service.SubscriptionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<SubscriptionResponse> create(@Valid @RequestBody CreateSubscriptionRequest request) {
        return ResponseEntity.ok(subscriptionService.create(request));
    }

    @GetMapping("/me")
    public ResponseEntity<List<SubscriptionResponse>> listMine() {
        return ResponseEntity.ok(subscriptionService.listMySubscriptions());
    }

    @GetMapping("/active")
    public ResponseEntity<SubscriptionResponse> getActive() {
        return ResponseEntity.ok(subscriptionService.getActiveSubscription());
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<SubscriptionResponse> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(subscriptionService.cancel(id));
    }
}