package com.evermynd.subscription.dto;

import com.evermynd.subscription.enums.Plan;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateSubscriptionRequest(
        @NotNull(message = "Plano é obrigatório")
        Plan plan,
        @NotBlank(message = "Token do cartão é obrigatório")
        String cardToken,
        @NotBlank(message = "Método de pagamento é obrigatório")
        String paymentMethodId
) {
}