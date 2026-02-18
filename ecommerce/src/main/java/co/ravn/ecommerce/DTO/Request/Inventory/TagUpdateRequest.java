package co.ravn.ecommerce.DTO.Request.Inventory;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TagUpdateRequest {
    private String name;

    public TagUpdateRequest(String name) {
        this.name = name;
    }

}
