package co.ravn.ecommerce.mappers.inventory;

import co.ravn.ecommerce.dto.response.inventory.ProductStockResponse;
import co.ravn.ecommerce.entities.inventory.ProductStock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductStockMapper {

    @Mapping(source = "product.id", target = "product_id")
    @Mapping(source = "warehouse.id", target = "warehouse_id")
    @Mapping(source = "lastUpdated", target = "last_updated")
    ProductStockResponse toResponse(ProductStock stock);
}
