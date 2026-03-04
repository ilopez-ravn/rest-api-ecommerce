package co.ravn.ecommerce.dto.graphql;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentIntentInput {

    private Integer shoppingCartId;
    private Integer warehouseId;
    private Integer addressId;
    private Double deliveryFee;
}

