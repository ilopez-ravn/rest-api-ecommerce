package co.ravn.ecommerce.Mappers.Cart;

import co.ravn.ecommerce.DTO.Response.Cart.CartProductResponse;
import co.ravn.ecommerce.DTO.Response.Cart.ShoppingCartResponse;
import co.ravn.ecommerce.Entities.Cart.ShoppingCart;
import co.ravn.ecommerce.Entities.Cart.ShoppingCartDetails;
import co.ravn.ecommerce.Mappers.Inventory.ProductMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ProductMapper.class})
public interface CartMapper {

    @Mapping(source = "client.id", target = "client_id")
    @Mapping(source = "status", target = "status")
    ShoppingCartResponse toResponse(ShoppingCart cart);

    @Mapping(target = "total",
            expression = "java(details.getPrice().multiply(java.math.BigDecimal.valueOf(details.getQuantity())))")
    CartProductResponse toDetailResponse(ShoppingCartDetails details);
}
