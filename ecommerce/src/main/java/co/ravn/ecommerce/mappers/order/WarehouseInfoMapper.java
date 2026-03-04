package co.ravn.ecommerce.mappers.order;

import co.ravn.ecommerce.dto.response.order.WarehouseInfoResponse;
import co.ravn.ecommerce.entities.inventory.Warehouse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WarehouseInfoMapper {

    @Mapping(source = "isActive", target = "is_active")
    @Mapping(source = "createdAt", target = "created_at")
    WarehouseInfoResponse toResponse(Warehouse warehouse);
}
