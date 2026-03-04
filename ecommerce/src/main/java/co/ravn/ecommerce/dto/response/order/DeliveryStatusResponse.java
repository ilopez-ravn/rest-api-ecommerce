package co.ravn.ecommerce.dto.response.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryStatusResponse {

    private int id;
    private String name;
    private Integer step_order;
    private String description;
}

