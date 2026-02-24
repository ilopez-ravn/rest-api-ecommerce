package co.ravn.ecommerce.Controllers.Cart;

import co.ravn.ecommerce.DTO.Request.Cart.CartProductRequest;
import co.ravn.ecommerce.DTO.Request.Cart.NewCartRequest;
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
    public ResponseEntity<?> createCart(@RequestBody @Valid NewCartRequest newCartRequest) {
        return cartService.createCart(newCartRequest);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCartById(@RequestParam @Min(1) int id) {
        return cartService.getCartById(id);
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<?> addItemToCart(@PathVariable @Min(1) int id, @RequestBody @Valid CartProductRequest cartProductRequest) {
        return cartService.addItemToCart(id, cartProductRequest);
    }

    @PutMapping("/{id}/items/{itemId}")
    public ResponseEntity<?> updateCartItem(@PathVariable @Min(1) int id, @PathVariable @Min(1) int itemId,
                                            @RequestBody @Valid CartProductRequest cartProductRequest) {
        return cartService.updateCartItem(id, itemId, cartProductRequest);
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<?> removeItemFromCart(@PathVariable @Min(1) int id, @PathVariable @Min(1) int itemId) {
        return cartService.removeItemFromCart(id, itemId);
    }

    @GetMapping("/clients/{id}")
    public ResponseEntity<?> getCartByClientId(@PathVariable @Min(1) int id) {
        return cartService.getCartByClientId(id);
    }

}
