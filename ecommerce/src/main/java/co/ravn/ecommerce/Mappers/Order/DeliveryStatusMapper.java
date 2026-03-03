package co.ravn.ecommerce.Mappers.Order;

import co.ravn.ecommerce.DTO.Response.Order.DeliveryStatusResponse;
import co.ravn.ecommerce.Entities.Order.DeliveryStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DeliveryStatusMapper {

    @Mapping(source = "stepOrder", target = "step_order")
    DeliveryStatusResponse toResponse(DeliveryStatus status);
}

