package co.ravn.ecommerce.DTO.Response.Order;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ShippingDetailsResponse {
    private DeliveryTrackingResponse delivery_tracking;
    private List<TrackingLogResponse> history;
    private AddressResponse address;
}
