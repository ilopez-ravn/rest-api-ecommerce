package co.ravn.ecommerce.DTO.Request.Inventory;

import com.fasterxml.jackson.annotation.JsonProperty;

import co.ravn.ecommerce.Entities.Inventory.StockOperationType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class AddStockRequest {
    @NotNull(message = "Product id is required")
    @Positive(message = "Product id must be positive")
    @JsonProperty("product_id")
    private Integer productId;

    @NotNull(message = "Operation type is required")
    private StockOperationType type;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}
