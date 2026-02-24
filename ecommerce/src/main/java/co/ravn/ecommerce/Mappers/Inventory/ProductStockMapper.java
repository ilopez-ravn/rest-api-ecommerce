package co.ravn.ecommerce.Mappers.Inventory;

import co.ravn.ecommerce.DTO.Response.Inventory.ProductStockResponse;
import co.ravn.ecommerce.Entities.Inventory.ProductStock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductStockMapper {

    @Mapping(source = "product.id", target = "product_id")
    @Mapping(source = "warehouse.id", target = "warehouse_id")
    @Mapping(source = "lastUpdated", target = "last_updated")
    ProductStockResponse toResponse(ProductStock stock);
}
