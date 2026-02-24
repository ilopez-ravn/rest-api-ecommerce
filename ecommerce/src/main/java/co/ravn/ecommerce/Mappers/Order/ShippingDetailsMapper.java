package co.ravn.ecommerce.Mappers.Order;

import co.ravn.ecommerce.DTO.Response.Order.ShippingDetailsResponse;
import co.ravn.ecommerce.Entities.Order.DeliveryTracking;
import co.ravn.ecommerce.Entities.Order.OrderTrackingLog;
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
