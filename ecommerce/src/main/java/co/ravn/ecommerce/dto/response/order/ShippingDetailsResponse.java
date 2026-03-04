package co.ravn.ecommerce.dto.response.order;

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
