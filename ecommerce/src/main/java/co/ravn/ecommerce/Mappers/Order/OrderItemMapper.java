package co.ravn.ecommerce.Mappers.Order;

import co.ravn.ecommerce.DTO.Response.Order.OrderItemResponse;
import co.ravn.ecommerce.Entities.Order.OrderDetails;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(source = "product.id", target = "product_id")
    @Mapping(source = "product.name", target = "product_name")
    @Mapping(source = "taxPercent", target = "tax_percent")
    @Mapping(source = "totalAmount", target = "total_amount")
    @Mapping(source = "taxAmount", target = "tax_amount")
    OrderItemResponse toResponse(OrderDetails detail);
}
