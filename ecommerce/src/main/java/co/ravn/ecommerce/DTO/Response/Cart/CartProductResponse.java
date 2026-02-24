package co.ravn.ecommerce.DTO.Response.Cart;

import co.ravn.ecommerce.DTO.Response.Inventory.ProductResponse;
import co.ravn.ecommerce.Entities.Cart.ShoppingCartDetails;
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
