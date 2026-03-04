package co.ravn.ecommerce.dto.response.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentIntentResponse {
    private String client_secret;
}
