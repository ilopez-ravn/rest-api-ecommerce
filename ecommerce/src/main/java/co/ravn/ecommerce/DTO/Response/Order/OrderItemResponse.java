package co.ravn.ecommerce.DTO.Response.Order;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class OrderItemResponse {
    private int product_id;
    private String product_name;
    private BigDecimal price;
    private Integer quantity;
    private Integer tax_percent;
    private BigDecimal total_amount;
    private BigDecimal tax_amount;
}
