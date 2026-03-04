package co.ravn.ecommerce.dto.response.cart;

import co.ravn.ecommerce.entities.cart.ShoppingCart;
import co.ravn.ecommerce.mappers.cart.CartMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ShoppingCartResponse {
    private int id;
    private int client_id;
    private String status;
    private List<CartProductResponse> products;

    public ShoppingCartResponse(ShoppingCart newCart, CartMapper cartMapper) {
        id = newCart.getId();
        client_id = newCart.getClient().getId();
        status = String.valueOf(newCart.getStatus());

        products = newCart.getProducts().stream()
            .map(cartMapper::toDetailResponse)
            .collect(Collectors.toList());
    }
}
