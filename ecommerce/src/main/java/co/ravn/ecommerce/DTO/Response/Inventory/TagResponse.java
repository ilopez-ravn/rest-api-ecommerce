package co.ravn.ecommerce.DTO.Response.Inventory;

import co.ravn.ecommerce.Entities.Inventory.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class TagResponse {
    @NotBlank
    private int id;
    @NotBlank
    private String name;
    @NotBlank
    private Boolean is_active;

    public TagResponse(Tag tag) {
        this.id = tag.getId();
        this.name = tag.getName();
        this.is_active = tag.getIsActive();
    }
}
