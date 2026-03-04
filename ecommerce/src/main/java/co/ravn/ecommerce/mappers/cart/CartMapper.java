package co.ravn.ecommerce.mappers.cart;

import co.ravn.ecommerce.dto.response.cart.CartProductResponse;
import co.ravn.ecommerce.dto.response.cart.ShoppingCartResponse;
import co.ravn.ecommerce.entities.cart.ShoppingCart;
import co.ravn.ecommerce.entities.cart.ShoppingCartDetails;
import co.ravn.ecommerce.mappers.inventory.ProductMapper;
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
