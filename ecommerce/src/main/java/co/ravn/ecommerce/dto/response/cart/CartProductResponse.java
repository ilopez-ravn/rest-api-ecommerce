package co.ravn.ecommerce.dto.response.cart;

import co.ravn.ecommerce.dto.response.inventory.ProductResponse;
import co.ravn.ecommerce.entities.cart.ShoppingCartDetails;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class CartProductResponse {
    private int id;
    private BigDecimal price;
    private int quantity;
    private BigDecimal total;
    private ProductResponse product;

    public CartProductResponse(ShoppingCartDetails shoppingCartDetails) {

    }
}
