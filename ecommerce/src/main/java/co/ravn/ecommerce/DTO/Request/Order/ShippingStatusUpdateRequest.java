package co.ravn.ecommerce.DTO.Request.Order;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ShippingStatusUpdateRequest {

    @JsonProperty("status_id")
    @NotNull
    private Integer statusId;
}
