package co.ravn.ecommerce.Controllers.Cart;

import co.ravn.ecommerce.DTO.Request.Cart.CartProductRequest;
import co.ravn.ecommerce.DTO.Request.Cart.NewCartRequest;
import co.ravn.ecommerce.DTO.Response.Cart.ShoppingCartResponse;
import co.ravn.ecommerce.Services.Cart.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@Validated
@RequestMapping("api/v1/carts")
public class CartController {

    private final CartService cartService;

    @PostMapping("")
    public ResponseEntity<ShoppingCartResponse> createCart(@RequestBody @Valid NewCartRequest newCartRequest) {
        return ResponseEntity.ok(cartService.createCart(newCartRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShoppingCartResponse> getCartById(@PathVariable @Min(1) int id) {
        return ResponseEntity.ok(cartService.getCartById(id));
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<ShoppingCartResponse> addItemToCart(@PathVariable @Min(1) int id, @RequestBody @Valid CartProductRequest cartProductRequest) {
        return ResponseEntity.ok(cartService.addItemToCart(id, cartProductRequest));
    }

    @PutMapping("/{id}/items/{itemId}")
    public ResponseEntity<ShoppingCartResponse> updateCartItem(@PathVariable @Min(1) int id, @PathVariable @Min(1) int itemId,
                                                               @RequestBody @Valid CartProductRequest cartProductRequest) {
        return ResponseEntity.ok(cartService.updateCartItem(id, itemId, cartProductRequest));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<ShoppingCartResponse> removeItemFromCart(@PathVariable @Min(1) int id, @PathVariable @Min(1) int itemId) {
        return ResponseEntity.ok(cartService.removeItemFromCart(id, itemId));
    }

    @GetMapping("/clients/{id}")
    public ResponseEntity<ShoppingCartResponse> getCartByClientId(@PathVariable @Min(1) int id) {
        return ResponseEntity.ok(cartService.getCartByClientId(id));
    }
}
