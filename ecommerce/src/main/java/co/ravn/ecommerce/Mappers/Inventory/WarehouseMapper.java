package co.ravn.ecommerce.Mappers.Inventory;

import co.ravn.ecommerce.DTO.Response.Inventory.WarehouseResponse;
import co.ravn.ecommerce.Entities.Inventory.Warehouse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WarehouseMapper {

    @Mapping(source = "isActive", target = "is_active")
    @Mapping(source = "createdAt", target = "created_at")
    @Mapping(source = "lastUpdated", target = "last_updated")
    WarehouseResponse toResponse(Warehouse warehouse);
}
