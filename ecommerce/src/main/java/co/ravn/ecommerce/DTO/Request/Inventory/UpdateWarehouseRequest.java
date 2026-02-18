package co.ravn.ecommerce.DTO.Request.Inventory;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class UpdateWarehouseRequest {
    @NotBlank
    private String name;

    private String location;
}
