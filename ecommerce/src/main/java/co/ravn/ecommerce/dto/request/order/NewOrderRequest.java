package co.ravn.ecommerce.dto.request.order;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NewOrderRequest {

    @JsonProperty("warehouse_id")
    @NotNull
    @Min(value = 1, message = "Warehouse id must be at least 1")
    private Integer warehouseId;

    @JsonProperty("cart_id")
    @NotNull
    @Min(value = 1, message = "Cart id must be at least 1")
    private Integer cartId;

    @JsonProperty("address_id")
    @NotNull
    @Min(value = 1, message = "Address id must be at least 1")
    private Integer addressId;
}
