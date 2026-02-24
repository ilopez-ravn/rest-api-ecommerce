package co.ravn.ecommerce.Mappers.Order;

import co.ravn.ecommerce.DTO.Response.Order.DeliveryTrackingResponse;
import co.ravn.ecommerce.Entities.Order.DeliveryTracking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DeliveryTrackingMapper {

    @Mapping(source = "order.id", target = "order_id")
    @Mapping(source = "address.id", target = "address_id")
    @Mapping(source = "trackingNumber", target = "tracking_number")
    @Mapping(source = "assignedTo.id", target = "assigned_to")
    @Mapping(source = "status.name", target = "status")
    @Mapping(source = "estimatedDeliveryDate", target = "estimated_delivery_date")
    DeliveryTrackingResponse toResponse(DeliveryTracking tracking);
}

