package co.ravn.ecommerce.DTO.Request.Cart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
public class NewCartRequest {
    @NotNull(message = "Products are required")
    List<CartProductRequest> products;
}
