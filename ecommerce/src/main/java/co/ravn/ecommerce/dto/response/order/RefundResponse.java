package co.ravn.ecommerce.dto.response.order;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class RefundResponse {
    private int id;
    private int order_id;
    private String status;
    private boolean requires_return;
    private String reason;
    private String manager_notes;
    private BigDecimal refund_amount;
    private String stripe_refund_id;
    private LocalDateTime requested_at;
    private LocalDateTime reviewed_at;
    private LocalDateTime refunded_at;
    private ReturnShipmentResponse return_shipment;
}
