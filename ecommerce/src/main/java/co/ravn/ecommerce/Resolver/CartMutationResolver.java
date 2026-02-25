package co.ravn.ecommerce.Resolver;

import co.ravn.ecommerce.DTO.Request.Cart.CartProductRequest;
import co.ravn.ecommerce.DTO.Request.Cart.NewCartRequest;
import co.ravn.ecommerce.DTO.Response.Cart.ShoppingCartResponse;
import co.ravn.ecommerce.Services.Cart.CartService;
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
    @PreAuthorize("hasRole('CLIENT')")
    @Transactional
    public ShoppingCartResponse createMyCart() {
        // Create an empty cart for the current client
        NewCartRequest request = new NewCartRequest(Collections.emptyList());
        return cartService.createCart(request);
    }

    @MutationMapping
    @PreAuthorize("hasRole('CLIENT')")
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
    @PreAuthorize("hasRole('CLIENT')")
    @Transactional
    public ShoppingCartResponse updateCartItem(@Argument int cartId,
                                               @Argument int productId,
                                               @Argument int quantity) {
        CartProductRequest request = new CartProductRequest(
                null,
                quantity,
                productId
        );
        // CartService.updateCartItem expects itemId, not productId; here we reuse cartId as id and productId as itemId
        return cartService.updateCartItem(cartId, productId, request);
    }

    @MutationMapping
    @PreAuthorize("hasRole('CLIENT')")
    @Transactional
    public ShoppingCartResponse removeItemFromCart(@Argument int cartId,
                                                   @Argument int productId) {
        // CartService.removeItemFromCart expects itemId; use productId as itemId here
        return cartService.removeItemFromCart(cartId, productId);
    }

    @MutationMapping
    @PreAuthorize("hasRole('CLIENT')")
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

