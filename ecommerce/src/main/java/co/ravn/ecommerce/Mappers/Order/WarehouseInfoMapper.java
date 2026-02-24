package co.ravn.ecommerce.Mappers.Order;

import co.ravn.ecommerce.DTO.Response.Order.WarehouseInfoResponse;
import co.ravn.ecommerce.Entities.Inventory.Warehouse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WarehouseInfoMapper {

    @Mapping(source = "isActive", target = "is_active")
    @Mapping(source = "createdAt", target = "created_at")
    WarehouseInfoResponse toResponse(Warehouse warehouse);
}
