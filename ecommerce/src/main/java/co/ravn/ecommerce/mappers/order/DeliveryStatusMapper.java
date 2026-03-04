package co.ravn.ecommerce.mappers.order;

import co.ravn.ecommerce.dto.response.order.DeliveryStatusResponse;
import co.ravn.ecommerce.entities.order.DeliveryStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DeliveryStatusMapper {

    @Mapping(source = "stepOrder", target = "step_order")
    DeliveryStatusResponse toResponse(DeliveryStatus status);
}

