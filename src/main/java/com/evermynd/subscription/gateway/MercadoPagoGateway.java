package com.evermynd.subscription.gateway;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.evermynd.subscription.enums.Plan;
import com.evermynd.subscription.exception.PaymentFailedException;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class MercadoPagoGateway implements PaymentGateway {

    private final RestTemplate restTemplate;

    @Value("${mercadopago.access-token}")
    private String accessToken;

    @Value("${mercadopago.base-url}")
    private String baseUrl;

    @Value("${subscription.price.monthly}")
    private double monthlyPrice;

    @Value("${subscription.price.annual}")
    private double annualPrice;

    public MercadoPagoGateway(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public PaymentResult charge(ChargeRequest request) {
        double amount = request.plan() == Plan.MONTHLY ? monthlyPrice : annualPrice;
        String description = request.plan() == Plan.MONTHLY
                ? "Assinatura EverMynd - Mensal"
                : "Assinatura EverMynd - Anual";

        PaymentRequestBody body = new PaymentRequestBody(
                amount,
                request.cardToken(),
                description,
                1,
                request.paymentMethodId(),
                new Payer(request.payerEmail()),
                request.externalReference()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        headers.set("X-Idempotency-Key", UUID.randomUUID().toString());

        HttpEntity<PaymentRequestBody> entity = new HttpEntity<>(body, headers);

        PaymentResponseBody response;
        try {
            response = restTemplate.postForObject(baseUrl + "/v1/payments", entity, PaymentResponseBody.class);
        } catch (HttpClientErrorException e) {
            throw new PaymentFailedException("Mercado Pago recusou a requisição: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new PaymentFailedException("Falha ao comunicar com o Mercado Pago: " + e.getMessage());
        }

        if (response == null) {
            throw new PaymentFailedException("Mercado Pago não retornou resposta");
        }

        boolean approved = "approved".equals(response.status());
        return new PaymentResult(approved, response.id() != null ? response.id().toString() : null, response.statusDetail());
    }

    private record Payer(String email) {
    }

    private record PaymentRequestBody(
            @JsonProperty("transaction_amount") double transactionAmount,
            String token,
            String description,
            Integer installments,
            @JsonProperty("payment_method_id") String paymentMethodId,
            Payer payer,
            @JsonProperty("external_reference") String externalReference
    ) {
    }

    private record PaymentResponseBody(
            Long id,
            String status,
            @JsonProperty("status_detail") String statusDetail
    ) {
    }
}