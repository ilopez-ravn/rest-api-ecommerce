package co.ravn.ecommerce.DTO.Request.Payment;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PaymentIntentRequest {

    @JsonProperty("shopping_cart_id")
    @NotNull
    @Min(value = 1, message = "Shopping cart id must be at least 1")
    private Integer shoppingCartId;

    @JsonProperty("warehouse_id")
    @NotNull
    @Min(value = 1, message = "Warehouse id must be at least 1")
    private Integer warehouseId;

    @JsonProperty("address_id")
    @NotNull
    @Min(value = 1, message = "Address id must be at least 1")
    private Integer addressId;

    @JsonProperty("delivery_fee")
    private BigDecimal deliveryFee = BigDecimal.ZERO;
}
