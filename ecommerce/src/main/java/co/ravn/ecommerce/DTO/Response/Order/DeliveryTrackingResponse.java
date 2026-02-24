package co.ravn.ecommerce.DTO.Response.Order;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class DeliveryTrackingResponse {
    private int id;
    private int order_id;
    private int address_id;
    private String tracking_number;
    private Integer assigned_to;
    private String status;
    private LocalDateTime estimated_delivery_date;
}
