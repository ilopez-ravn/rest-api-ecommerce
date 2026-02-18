package co.ravn.ecommerce.DTO.Request.Inventory;

import com.fasterxml.jackson.annotation.JsonProperty;

import co.ravn.ecommerce.Entities.Inventory.StockOperationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class AddStockRequest {
    @NotBlank
    @JsonProperty("product_id")
    private int productId;

    @NotNull
    private StockOperationType type;

    @NotBlank
    private int quantity;
}
