package co.ravn.ecommerce.dto.response.order;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ReturnShipmentResponse {
    private String tracking_number;
    private String carrier_name;
    private LocalDateTime shipped_at;
    private LocalDateTime received_at;
}
