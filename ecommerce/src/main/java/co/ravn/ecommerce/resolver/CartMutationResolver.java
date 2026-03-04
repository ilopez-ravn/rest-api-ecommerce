package co.ravn.ecommerce.resolver;

import co.ravn.ecommerce.dto.request.cart.CartProductRequest;
import co.ravn.ecommerce.dto.request.cart.NewCartRequest;
import co.ravn.ecommerce.dto.response.cart.ShoppingCartResponse;
import co.ravn.ecommerce.services.cart.CartService;
import lombok.AllArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Controller
@AllArgsConstructor
public class CartMutationResolver {

    private final CartService cartService;

    @MutationMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'MANAGER')")
    @Transactional
    public ShoppingCartResponse createMyCart() {
        // Create an empty cart for the current client
        NewCartRequest request = new NewCartRequest(Collections.emptyList());
        return cartService.createCart(request);
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'MANAGER')")
    @Transactional
    public ShoppingCartResponse addItemToCart(@Argument int cartId,
                                              @Argument int productId,
                                              @Argument int quantity) {
        CartProductRequest request = new CartProductRequest(
                null,
                quantity,
                productId
        );
        return cartService.addItemToCart(cartId, request);
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'MANAGER')")
    @Transactional
    public ShoppingCartResponse updateCartItem(@Argument int cartId,
                                               @Argument int itemId,
                                               @Argument int quantity) {
        CartProductRequest request = new CartProductRequest(
                null,
                quantity,
                itemId
        );
        return cartService.updateCartItem(cartId, itemId, request);
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'MANAGER')")
    @Transactional
    public ShoppingCartResponse removeItemFromCart(@Argument int cartId,
                                                   @Argument int productId) {
        // CartService.removeItemFromCart expects itemId; use productId as itemId here
        return cartService.removeItemFromCart(cartId, productId);
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'MANAGER')")
    @Transactional
    public ShoppingCartResponse clearCart(@Argument int cartId) {
        ShoppingCartResponse cart = cartService.getCartById(cartId);
        if (cart.getProducts() == null || cart.getProducts().isEmpty()) {
            return cart;
        }
        // Remove each item from the cart
        cart.getProducts().forEach(item ->
                cartService.removeItemFromCart(cartId, item.getId())
        );
        return cartService.getCartById(cartId);
    }
}

