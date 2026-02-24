package co.ravn.ecommerce.Mappers.Order;

import co.ravn.ecommerce.DTO.Response.Order.TrackingLogResponse;
import co.ravn.ecommerce.Entities.Order.OrderTrackingLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrackingLogMapper {

    @Mapping(source = "previousStatus.name", target = "previous_status")
    @Mapping(source = "newStatus.name", target = "new_status")
    @Mapping(source = "changedBy.id", target = "changed_by")
    @Mapping(source = "changedAt", target = "changed_at")
    TrackingLogResponse toResponse(OrderTrackingLog log);
}
