package com.evermynd.subscription.exception;

public class ActiveSubscriptionAlreadyExistsException extends RuntimeException {
    public ActiveSubscriptionAlreadyExistsException(String message) {
        super(message);
    }
}