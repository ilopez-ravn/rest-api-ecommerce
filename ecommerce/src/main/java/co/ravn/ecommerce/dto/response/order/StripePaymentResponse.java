package co.ravn.ecommerce.dto.response.order;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class StripePaymentResponse {
    private int id;
    private int order_id;
    private String payment_type;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String payment_method;
}
