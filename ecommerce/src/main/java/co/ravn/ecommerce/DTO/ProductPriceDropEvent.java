package co.ravn.ecommerce.DTO;

import java.math.BigDecimal;

public record ProductPriceDropEvent(
        int productId,
        String productName,
        BigDecimal oldPrice,
        BigDecimal newPrice
) {
}

