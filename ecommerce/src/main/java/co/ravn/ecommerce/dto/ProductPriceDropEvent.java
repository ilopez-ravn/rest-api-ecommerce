package co.ravn.ecommerce.dto;

import java.math.BigDecimal;

public record ProductPriceDropEvent(
        int productId,
        String productName,
        BigDecimal oldPrice,
        BigDecimal newPrice
) {
}

