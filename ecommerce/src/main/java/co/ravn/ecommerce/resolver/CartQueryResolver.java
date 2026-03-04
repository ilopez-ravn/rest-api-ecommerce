package co.ravn.ecommerce.resolver;

import co.ravn.ecommerce.dto.response.cart.ShoppingCartResponse;
import co.ravn.ecommerce.services.cart.CartService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
@AllArgsConstructor
public class CartQueryResolver {

    private final CartService cartService;

    @QueryMapping
    @PreAuthorize("hasAnyRole('MANAGER','CLIENT')")
    public ShoppingCartResponse getCartById(@Argument int id) {
        log.info("Getting cart by id: " + id);
        return cartService.getCartById(id);
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('MANAGER','CLIENT')")
    public ShoppingCartResponse getClientCart(@Argument int clientId) {
        log.info("Getting cart for client id: " + clientId);
        return cartService.getCartByClientId(clientId);
    }

    @QueryMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ShoppingCartResponse getMyCart() {
        // Current user is inferred inside CartService.getCartByClientId
        // Passing 0 triggers access-check logic; method will enforce ownership
        log.info("Getting my cart");
        return cartService.getCartByClientId(0);
    }
}

