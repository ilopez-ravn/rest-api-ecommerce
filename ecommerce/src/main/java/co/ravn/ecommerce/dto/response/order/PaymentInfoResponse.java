package co.ravn.ecommerce.dto.response.order;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class PaymentInfoResponse {
    private String status;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
}
