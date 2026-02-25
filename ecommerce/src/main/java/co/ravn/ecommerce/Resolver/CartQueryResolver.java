package co.ravn.ecommerce.Resolver;

import co.ravn.ecommerce.DTO.Response.Cart.ShoppingCartResponse;
import co.ravn.ecommerce.Services.Cart.CartService;
import lombok.AllArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
@AllArgsConstructor
public class CartQueryResolver {

    private final CartService cartService;

    @QueryMapping
    @PreAuthorize("hasAnyRole('MANAGER','CLIENT')")
    public ShoppingCartResponse getCartById(@Argument int id) {
        return cartService.getCartById(id);
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('MANAGER','CLIENT')")
    public ShoppingCartResponse getClientCart(@Argument int clientId) {
        return cartService.getCartByClientId(clientId);
    }

    @QueryMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ShoppingCartResponse getMyCart() {
        // Current user is inferred inside CartService.getCartByClientId
        // Passing 0 triggers access-check logic; method will enforce ownership
        return cartService.getCartByClientId(0);
    }
}

