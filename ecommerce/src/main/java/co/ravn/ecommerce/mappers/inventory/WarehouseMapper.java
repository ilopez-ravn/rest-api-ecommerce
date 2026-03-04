package co.ravn.ecommerce.mappers.inventory;

import co.ravn.ecommerce.dto.response.inventory.WarehouseResponse;
import co.ravn.ecommerce.entities.inventory.Warehouse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WarehouseMapper {

    @Mapping(source = "isActive", target = "is_active")
    @Mapping(source = "createdAt", target = "created_at")
    @Mapping(source = "lastUpdated", target = "last_updated")
    WarehouseResponse toResponse(Warehouse warehouse);
}
