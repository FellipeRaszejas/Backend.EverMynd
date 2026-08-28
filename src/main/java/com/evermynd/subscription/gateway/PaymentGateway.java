package com.evermynd.subscription.gateway;

import com.evermynd.subscription.enums.Plan;

public interface PaymentGateway {
    PaymentResult charge(ChargeRequest request);

    record ChargeRequest(
            String cardToken,
            String paymentMethodId,
            String payerEmail,
            Plan plan,
            String externalReference
    ) {
    }

    record PaymentResult(boolean approved, String paymentId, String statusDetail) {
    }
}