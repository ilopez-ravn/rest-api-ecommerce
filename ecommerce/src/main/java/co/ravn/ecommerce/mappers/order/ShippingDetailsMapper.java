package co.ravn.ecommerce.mappers.order;

import co.ravn.ecommerce.dto.response.order.ShippingDetailsResponse;
import co.ravn.ecommerce.entities.order.DeliveryTracking;
import co.ravn.ecommerce.entities.order.OrderTrackingLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {
        DeliveryTrackingMapper.class,
        AddressMapper.class,
        TrackingLogMapper.class
})
public interface ShippingDetailsMapper {

    @Mapping(target = "delivery_tracking", source = "tracking")
    @Mapping(target = "history", source = "history")
    @Mapping(target = "address", source = "tracking.address")
    ShippingDetailsResponse toResponse(DeliveryTracking tracking, List<OrderTrackingLog> history);
}
