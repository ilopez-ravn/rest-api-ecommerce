package co.ravn.ecommerce.DTO.Response.Order;

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
