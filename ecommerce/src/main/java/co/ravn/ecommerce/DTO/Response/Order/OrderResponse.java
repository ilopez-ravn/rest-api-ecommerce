package co.ravn.ecommerce.DTO.Response.Order;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OrderResponse {
    private int id;
    private LocalDateTime order_date;
    private BillingInfoResponse bill;
    private AddressResponse address;
    private StripePaymentResponse payment;
    private ClientInfoResponse client;
    private WarehouseInfoResponse warehouse;
    private DeliveryTrackingResponse tracking;
    private List<OrderItemResponse> items;
}
