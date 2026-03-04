package co.ravn.ecommerce.dto.response.order;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class BillingInfoResponse {
    private String document_number;
    private String document_type;
    private BigDecimal total_amount;
    private BigDecimal tax_amount;
    private BigDecimal delivery_fee;
}
