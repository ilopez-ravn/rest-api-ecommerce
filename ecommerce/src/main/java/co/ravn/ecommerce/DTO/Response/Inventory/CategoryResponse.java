package co.ravn.ecommerce.DTO.Response.Inventory;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CategoryResponse {
    private int id;
    private String name;
    private String description;
    private Boolean is_active;
}
