package co.ravn.ecommerce.DTO.Response.Inventory;

import co.ravn.ecommerce.Entities.Inventory.Category;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class CategoryResponse {
    @NotBlank
    private int id;
    @NotBlank
    private String name;
    private String description;
    @NotBlank
    private boolean is_active;

    public CategoryResponse(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.description = category.getDescription();
        this.is_active = category.isActive();
    }
}
