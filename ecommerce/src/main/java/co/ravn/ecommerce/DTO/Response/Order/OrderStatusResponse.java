package co.ravn.ecommerce.DTO.Response.Order;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderStatusResponse {
    private String status;
    private String refund_reason;
    private OrderResponse order;
}
